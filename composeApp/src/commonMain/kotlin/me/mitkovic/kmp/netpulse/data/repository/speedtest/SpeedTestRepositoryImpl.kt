package me.mitkovic.kmp.netpulse.data.repository.speedtest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.toDomainModel
import me.mitkovic.kmp.netpulse.data.remote.RemoteService
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.domain.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SpeedTestRepositoryImpl(
    private val localStorage: LocalStorage,
    private val remoteService: RemoteService,
    private val logger: AppLogger,
) : SpeedTestRepository {

    override fun getServers(): Flow<Resource<ServersResponse?>> =
        flow {
            try {
                localStorage.serverStorage.retrieveServers().collect { localResponse ->
                    val domainResponse = localResponse?.toDomainModel()
                    emit(Resource.Success(domainResponse))
                }
            } catch (e: Exception) {
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

    override fun syncServers(): Flow<Resource<ServersResponse?>> =
        flow {
            emit(Resource.Loading)
            try {
                remoteService.fetchSpeedTestServers().collect { remoteResult ->
                    when (remoteResult) {
                        is Resource.Success -> {
                            logger.logError(
                                tag = SpeedTestRepositoryImpl::class.simpleName,
                                message = "refreshServers servers: ${remoteResult.data}",
                                throwable = null,
                            )

                            localStorage.serverStorage.storeServers(response = remoteResult.data)
                            emit(Resource.Success(remoteResult.data.toDomainModel()))
                        }
                        is Resource.Error -> {
                            logger.logError(
                                tag = SpeedTestRepositoryImpl::class.simpleName,
                                message = "Remote error: ${remoteResult.throwable?.message}",
                                throwable = remoteResult.throwable,
                            )
                            emit(Resource.Error(remoteResult.throwable?.message ?: "Unknown error", remoteResult.throwable))
                        }
                        is Resource.Loading -> {
                            emit(Resource.Loading)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.logError(
                    tag = SpeedTestRepositoryImpl::class.simpleName,
                    message = "Error refreshing servers: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error", e))
            }
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

    override suspend fun executeSpeedTest(server: Server) {
        logger.logDebug(SpeedTestRepositoryImpl::class.simpleName, "Running speed test for server: ${server.name}")
        remoteService.performSpeedTest(server, localStorage)
    }

    override fun observeLatestTestResult(): Flow<Resource<TestResult?>> =
        flow {
            try {
                localStorage.testResultStorage
                    .getLatestTestResult()
                    .map {
                        Resource.Success(it)
                    }.collect { emit(it) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    logger.logDebug(
                        tag = SpeedTestRepositoryImpl::class.simpleName,
                        message = "Observing speed test results cancelled",
                    )
                } else {
                    logger.logError(
                        tag = SpeedTestRepositoryImpl::class.simpleName,
                        message = "Error observing speed test result: ${e.message}",
                        throwable = e,
                    )
                    emit(Resource.Error(e.message ?: "Unknown error", null))
                }
            }
        }
}
