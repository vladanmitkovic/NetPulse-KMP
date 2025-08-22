package me.mitkovic.kmp.netpulse.data.repository.speedtest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.common.Constants.SOMETHING_WENT_WRONG
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.local.database.TestSession
import me.mitkovic.kmp.netpulse.data.model.PingResult
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.data.model.toDomainModel
import me.mitkovic.kmp.netpulse.data.remote.RemoteService
import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepository
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
    private val settingsRepository: SettingsRepository,
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

    @OptIn(ExperimentalTime::class)
    override suspend fun fetchAndSaveUserLocation(): UserLocation? {
        try {
            localStorage.locationStorage.clearCurrentLocation()
            val response =
                remoteService.getUserLocation() ?: return null.also {
                    logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "Unable to get user location")
                }
            val timestamp =
                Clock.System
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
                    is Resource.Success -> processSuccessfulFetch(remoteResult.data)
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

    private suspend fun processSuccessfulFetch(remoteData: DataServersResponse?): Resource<ServersResponse?> {
        if (remoteData == null) {
            return Resource.Success(null)
        }
        logger.logError(
            tag = SpeedTestRepositoryImpl::class.simpleName,
            message = "refreshServers servers: $remoteData",
            throwable = null,
        )
        val currentLocation = localStorage.locationStorage.retrieveCurrentLocation().firstOrNull()
        val modifiedServers =
            remoteData.servers.map { server ->
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
        return Resource.Success(modifiedResponse.toDomainModel())
    }

    override suspend fun findLowestLatencyServer(): Server? {
        val servers = getLocalServers() ?: return null
        return remoteService.findNearestServer(servers).also { server ->
            if (server == null) logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No nearest server found")
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun executeSpeedTest(server: Server): Flow<SpeedTestProgress> =
        flow {
            try {
                val testDuration = settingsRepository.getTestDuration().firstOrNull() ?: 10
                val numPings = settingsRepository.getNumberOfPings().firstOrNull() ?: 10

                val timestamp = Clock.System.now().toEpochMilliseconds()
                val currentLocation = localStorage.locationStorage.retrieveCurrentLocation().firstOrNull()
                val effectiveLocation = currentLocation ?: UserLocation.default(timestamp)
                val testLocationId = localStorage.locationStorage.getOrStoreTestLocation(effectiveLocation)
                val sessionId = createTestSession(server, testLocationId, timestamp)

                val pingResult = performPingTest(server, numPings)
                emit(SpeedTestProgress(ping = pingResult.averageLatency, jitter = pingResult.jitter, packetLoss = pingResult.packetLoss))
                localStorage.testResultStorage.updateTestSessionPingJitterPacketLoss(
                    sessionId,
                    pingResult.averageLatency,
                    pingResult.jitter,
                    pingResult.packetLoss,
                )

                // Collect download progress from the flow
                performDownloadTest(server, sessionId, testDuration).collect { progress ->
                    emit(progress)
                }

                // Collect upload progress from the flow
                performUploadTest(server, sessionId, testDuration).collect { progress ->
                    emit(progress)
                }

                emit(SpeedTestProgress(isCompleted = true))
            } catch (e: Exception) {
                emit(SpeedTestProgress(error = e.message ?: SOMETHING_WENT_WRONG))
            }
        }

    private suspend fun createTestSession(
        server: Server,
        testLocationId: Long,
        timestamp: Long,
    ): Long =
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
            packetLoss = null,
            testTimestamp = timestamp,
        )

    private suspend fun performPingTest(
        server: Server,
        count: Int = 10,
    ): PingResult = remoteService.measurePingAndJitter(server, count)

    @OptIn(ExperimentalTime::class)
    private fun performDownloadTest(
        server: Server,
        sessionId: Long,
        duration: Int,
    ): Flow<SpeedTestProgress> =
        flow {
            val initialImageSize = "1000"
            remoteService.downloadTestMultiThread(server, initialImageSize, duration.toDouble()) { speed ->
                if (speed >= 0) {
                    val adjustedSpeed = speed * 8
                    emit(SpeedTestProgress(downloadSpeed = adjustedSpeed))
                    localStorage.testResultStorage.insertTestResult(
                        sessionId,
                        1,
                        adjustedSpeed,
                        Clock.System.now().toEpochMilliseconds(),
                    )
                }
            }
        }

    @OptIn(ExperimentalTime::class)
    private fun performUploadTest(
        server: Server,
        sessionId: Long,
        duration: Int,
    ): Flow<SpeedTestProgress> =
        flow {
            val initialPayloadSize = 128 * 1024 // 128 KB
            remoteService.uploadTestMultiThread(server, initialPayloadSize, duration.toDouble()) { speed ->
                if (speed >= 0) {
                    val adjustedSpeed = speed * 8
                    emit(SpeedTestProgress(uploadSpeed = adjustedSpeed))
                    localStorage.testResultStorage.insertTestResult(
                        sessionId,
                        2,
                        adjustedSpeed,
                        Clock.System.now().toEpochMilliseconds(),
                    )
                }
            }
        }

    override suspend fun findClosestServerByDistance(): Server? {
        val servers = getLocalServers() ?: return null
        return servers
            .minByOrNull { it.distance ?: Double.MAX_VALUE }
            .also { server ->
                if (server == null) logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No closest server found")
            }
    }

    override suspend fun getSortedServersByDistance(): List<Server> {
        val servers = getLocalServers() ?: return emptyList()
        return servers.sortedBy { it.distance ?: Double.MAX_VALUE }
    }

    private suspend fun getLocalServers(): List<Server>? {
        val servers =
            localStorage.serverStorage
                .retrieveServers()
                .firstOrNull()
                ?.toDomainModel()
                ?.servers
        if (servers.isNullOrEmpty()) {
            logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "No servers available")
            return null
        }
        return servers
    }

    override fun getTestSessions(): Flow<List<TestSession>> = localStorage.testResultStorage.getTestSessions()

    override fun getTestResultsBySessionId(sessionId: Long): Flow<List<TestResult>> =
        localStorage.testResultStorage.getTestResultsBySessionId(sessionId)

    override suspend fun deleteTestSession(sessionId: Long) {
        localStorage.testResultStorage.deleteTestSession(sessionId)
    }
}
