package me.mitkovic.kmp.netpulse.data.repository.speedtestservers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.local.database.SpeedTestResultEntity
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.toDomainModel
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSource
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.domain.model.SpeedTestServersResponse
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestServersRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SpeedTestServersRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val logger: AppLogger,
) : SpeedTestServersRepository {

    override fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>> =
        flow {
            try {
                localDataSource.speedTestServers.getSpeedTestServers().collect { localResponse ->
                    val domainResponse = localResponse?.toDomainModel()
                    emit(Resource.Success(domainResponse))
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    logger.logDebug(
                        tag = SpeedTestServersRepositoryImpl::class.simpleName,
                        message = "Server fetch cancelled",
                    )
                } else {
                    logger.logError(
                        tag = SpeedTestServersRepositoryImpl::class.simpleName,
                        message = "Error fetching local speed test servers: ${e.message}",
                        throwable = e,
                    )
                    emit(Resource.Error(e.message ?: "Unknown error", e))
                }
            }
        }

    override suspend fun getSpeedTestServer(serverId: Int): Server? =
        try {
            val response = localDataSource.speedTestServers.getSpeedTestServer(serverId).firstOrNull()
            response?.toDomainModel()?.servers?.firstOrNull()
        } catch (e: Exception) {
            logger.logError(
                tag = SpeedTestServersRepositoryImpl::class.simpleName,
                message = "Error fetching server with ID $serverId: ${e.message}",
                throwable = e,
            )
            null
        }

    override fun refreshSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>> =
        flow {
            emit(Resource.Loading)
            try {
                remoteDataSource.getSpeedTestServers().collect { remoteResult ->
                    when (remoteResult) {
                        is Resource.Success -> {
                            // Save to local storage
                            localDataSource.speedTestServers.saveSpeedTestServers(response = remoteResult.data)
                            emit(Resource.Success(remoteResult.data.toDomainModel()))
                        }
                        is Resource.Error -> {
                            logger.logError(
                                tag = SpeedTestServersRepositoryImpl::class.simpleName,
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
                    tag = SpeedTestServersRepositoryImpl::class.simpleName,
                    message = "Error refreshing speed test servers: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error", e))
            }
        }

    override suspend fun findNearestServer(): Server? {
        val servers =
            localDataSource.speedTestServers
                .getSpeedTestServers()
                .firstOrNull()
                ?.toDomainModel()
                ?.servers ?: return null.also {
                logger.logDebug(SpeedTestServersRepositoryImpl::class.simpleName, "No servers available")
            }
        return remoteDataSource.findNearestServer(servers).also { server ->
            if (server == null) logger.logDebug(SpeedTestServersRepositoryImpl::class.simpleName, "No nearest server found")
        }
    }

    override suspend fun runSpeedTest(server: Server) {
        logger.logDebug(SpeedTestServersRepositoryImpl::class.simpleName, "Running speed test for server: ${server.name}")
        remoteDataSource.runSpeedTest(server, localDataSource)
    }

    override fun observeSpeedTestResults(): Flow<Resource<SpeedTestResultEntity?>> =
        flow {
            try {
                localDataSource.speedTestResults
                    .getLatestSpeedTestResult()
                    .map {
                        // logger.logDebug(SpeedTestServersRepositoryImpl::class.simpleName, "Emitting resource: $it")
                        Resource.Success(it)
                    }.collect { emit(it) }
            } catch (e: Exception) {
                logger.logError(
                    tag = SpeedTestServersRepositoryImpl::class.simpleName,
                    message = "Error observing speed test result: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error", null))
            }
        }
}
