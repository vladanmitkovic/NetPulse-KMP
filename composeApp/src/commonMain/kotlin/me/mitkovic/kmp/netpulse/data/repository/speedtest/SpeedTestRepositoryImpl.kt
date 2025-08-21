package me.mitkovic.kmp.netpulse.data.repository.speedtest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.common.Constants.SOMETHING_WENT_WRONG
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.local.database.TestSession
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.data.model.toDomainModel
import me.mitkovic.kmp.netpulse.data.remote.RemoteService
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.domain.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.UserLocation
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.util.haversineDistance
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import me.mitkovic.kmp.netpulse.data.model.Server as DataServer
import me.mitkovic.kmp.netpulse.data.model.ServersResponse as DataServersResponse

class SpeedTestRepositoryImpl(
    private val localStorage: LocalStorage,
    private val remoteService: RemoteService,
    private val logger: AppLogger,
) : SpeedTestRepository {

    override fun getServers(): Flow<Resource<ServersResponse?>> =
        localStorage.serverStorage
            .retrieveServers()
            .map { serversResponse ->
                val result: Resource<ServersResponse?> = Resource.Success(serversResponse?.toDomainModel())
                result
            }.catch { e ->
                if (e is kotlinx.coroutines.CancellationException) {
                    logger.logDebug(
                        tag = SpeedTestRepositoryImpl::class.simpleName,
                        message = "Server fetch cancelled",
                    )
                } else {
                    logger.logError(
                        tag = SpeedTestRepositoryImpl::class.simpleName,
                        message = "Error fetching local servers: ${e.message}",
                        throwable = e,
                    )
                    emit(Resource.Error(e.message ?: "Unknown error", e))
                }
            }

    override suspend fun getServer(serverId: Int): Server? =
        try {
            val response = localStorage.serverStorage.getServer(serverId).firstOrNull()
            response?.toDomainModel()?.servers?.firstOrNull()
        } catch (e: Exception) {
            logger.logError(
                tag = SpeedTestRepositoryImpl::class.simpleName,
                message = "Error fetching server with ID $serverId: ${e.message}",
                throwable = e,
            )
            null
        }

    override suspend fun fetchAndSaveUserLocation(): UserLocation? {
        try {
            localStorage.locationStorage.clearCurrentLocation()
            val response =
                remoteService.getUserLocation() ?: return null.also {
                    logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "Unable to get user location")
                }
            val timestamp =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds()
            localStorage.locationStorage.storeCurrentLocation(response, timestamp)
            return response.toDomainModel(timestamp)
        } catch (e: Exception) {
            logger.logError(SpeedTestRepositoryImpl::class.simpleName, "Error fetching and saving user location: ${e.message}", e)
            return null
        }
    }

    override suspend fun syncServers(): Flow<Resource<ServersResponse?>> =
        remoteService
            .fetchSpeedTestServers()
            .map { remoteResult ->
                when (remoteResult) {
                    is Resource.Success -> {
                        logger.logError(
                            tag = SpeedTestRepositoryImpl::class.simpleName,
                            message = "refreshServers servers: ${remoteResult.data}",
                            throwable = null,
                        )
                        val currentLocation = localStorage.locationStorage.retrieveCurrentLocation().firstOrNull()
                        val modifiedServers =
                            remoteResult.data.servers.map { server ->
                                val lat = server.attrs["lat"]?.toDouble() ?: 0.0
                                val lon = server.attrs["lon"]?.toDouble() ?: 0.0
                                val dist =
                                    if (currentLocation != null) {
                                        (haversineDistance(currentLocation.latitude, currentLocation.longitude, lat, lon) * 1000)
                                    } else {
                                        0.0
                                    }
                                DataServer(
                                    attrs =
                                        server.attrs.toMutableMap().apply {
                                            put("distance", dist.toString())
                                        },
                                )
                            }
                        val modifiedResponse = DataServersResponse(servers = modifiedServers)
                        localStorage.serverStorage.storeServers(response = modifiedResponse)
                        Resource.Success(modifiedResponse.toDomainModel())
                    }
                    is Resource.Error -> {
                        logger.logError(
                            tag = SpeedTestRepositoryImpl::class.simpleName,
                            message = "Remote error: ${remoteResult.throwable?.message}",
                            throwable = remoteResult.throwable,
                        )
                        Resource.Error(remoteResult.throwable?.message ?: "Unknown error", remoteResult.throwable)
                    }
                    is Resource.Loading -> Resource.Loading
                }
            }.catch { e ->
                logger.logError(
                    tag = SpeedTestRepositoryImpl::class.simpleName,
                    message = "Error refreshing servers: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error", e))
            }

    override suspend fun findLowestLatencyServer(): Server? {
        val servers =
            localStorage.serverStorage
                .retrieveServers()
                .firstOrNull()
                ?.toDomainModel()
                ?.servers ?: return null.also {
                logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No servers available")
            }
        return remoteService.findNearestServer(servers).also { server ->
            if (server == null) logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No nearest server found")
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun executeSpeedTest(server: Server): Flow<SpeedTestProgress> =
        flow {
            try {
                val currentLocation = localStorage.locationStorage.retrieveCurrentLocation().firstOrNull()
                val timestamp = Clock.System.now().toEpochMilliseconds()

                val effectiveLocation =
                    currentLocation ?: UserLocation(
                        ip = null,
                        network = null,
                        version = null,
                        city = null,
                        region = null,
                        regionCode = null,
                        country = null,
                        countryName = null,
                        countryCode = null,
                        countryCodeIso3 = null,
                        countryCapital = null,
                        countryTld = null,
                        continentCode = null,
                        inEu = null,
                        postal = null,
                        latitude = 0.0,
                        longitude = 0.0,
                        timezone = null,
                        utcOffset = null,
                        countryCallingCode = null,
                        currency = null,
                        currencyName = null,
                        languages = null,
                        countryArea = null,
                        countryPopulation = null,
                        asn = null,
                        org = null,
                        timestamp = timestamp,
                    )

                val testLocationId = localStorage.locationStorage.getOrStoreTestLocation(effectiveLocation)

                val sessionId =
                    localStorage.testResultStorage.insertTestSession(
                        serverId = server.id.toString(),
                        serverUrl = server.url,
                        serverName = server.name,
                        serverCountry = server.country,
                        serverSponsor = server.sponsor,
                        serverHost = server.host,
                        serverDistance = server.distance ?: 0.0,
                        testLocationId = testLocationId,
                        ping = null,
                        jitter = null,
                        testTimestamp = timestamp,
                    )

                val pingResult = remoteService.measurePingAndJitter(server)

                emit(SpeedTestProgress(ping = pingResult.averageLatency, jitter = pingResult.jitter))

                localStorage.testResultStorage.updateTestSessionPingJitter(sessionId, pingResult.averageLatency, pingResult.jitter)

                val initialImageSize = "1000"

                remoteService.downloadTestMultiThread(server, initialImageSize, Constants.DOWNLOAD_TIMEOUT) { speed ->
                    if (speed >= 0) {
                        emit(SpeedTestProgress(downloadSpeed = speed * 8))
                        localStorage.testResultStorage.insertTestResult(sessionId, 1, speed * 8, Clock.System.now().toEpochMilliseconds())
                    }
                }

                val initialPayloadSize = 128 * 1024 // 128 KB

                remoteService.uploadTestMultiThread(server, initialPayloadSize, Constants.UPLOAD_TIMEOUT) { speed ->
                    if (speed >= 0) {
                        emit(SpeedTestProgress(uploadSpeed = speed * 8))
                        localStorage.testResultStorage.insertTestResult(sessionId, 2, speed * 8, Clock.System.now().toEpochMilliseconds())
                    }
                }

                emit(SpeedTestProgress(isCompleted = true))
            } catch (e: Exception) {
                emit(SpeedTestProgress(error = e.message ?: SOMETHING_WENT_WRONG))
            }
        }

    override suspend fun findClosestServerByDistance(): Server? {
        val servers =
            localStorage.serverStorage
                .retrieveServers()
                .firstOrNull()
                ?.toDomainModel()
                ?.servers ?: return null.also {
                logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No servers available")
            }
        return servers
            .minByOrNull { it.distance ?: Double.MAX_VALUE }
            .also { server ->
                if (server == null) logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No closest server found")
            }
    }

    override suspend fun getSortedServersByDistance(): List<Server> {
        val servers =
            localStorage.serverStorage
                .retrieveServers()
                .firstOrNull()
                ?.toDomainModel()
                ?.servers ?: return emptyList<Server>().also {
                logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No servers available")
            }
        return servers.sortedBy { it.distance ?: Double.MAX_VALUE }
    }

    override fun getTestSessions(): Flow<List<TestSession>> = localStorage.testResultStorage.getTestSessions()

    override fun getTestResultsBySessionId(sessionId: Long): Flow<List<TestResult>> =
        localStorage.testResultStorage.getTestResultsBySessionId(sessionId)
}
