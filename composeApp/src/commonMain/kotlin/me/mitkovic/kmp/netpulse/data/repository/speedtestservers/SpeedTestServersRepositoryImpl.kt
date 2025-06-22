package me.mitkovic.kmp.netpulse.data.repository.speedtestservers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.toDomainModel
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSource
import me.mitkovic.kmp.netpulse.domain.model.SpeedTestServersResponse
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestServersRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SpeedTestServersRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val logger: AppLogger,
) : SpeedTestServersRepository {

    override fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>> {
        TODO("Not yet implemented")
    }

    override fun refreshSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>> =
        flow {
            emit(Resource.Loading)
            try {
                remoteDataSource.getSpeedTestServers().collect { remoteResult ->
                    when (remoteResult) {
                        is Resource.Success -> {
                            logger.logDebug(
                                SpeedTestServersRepositoryImpl::class.simpleName,
                                "Remote data fetched: ${remoteResult.data}",
                            )
                            remoteResult.data?.let { data ->
                                // localDataSource.saveSpeedTestServers(data) // Save to local storage
                                emit(Resource.Success(data.toDomainModel()))
                            } ?: run {
                                logger.logDebug(
                                    SpeedTestServersRepositoryImpl::class.simpleName,
                                    "No remote data received",
                                )
                                emit(Resource.Success(null))
                            }
                        }
                        is Resource.Error -> {
                            logger.logError(
                                SpeedTestServersRepositoryImpl::class.simpleName,
                                "Remote error: ${remoteResult.throwable?.message}",
                                remoteResult.throwable,
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
                    SpeedTestServersRepositoryImpl::class.simpleName,
                    "Error refreshing speed test servers: ${e.message}",
                    e,
                )
                emit(Resource.Error(e.message ?: "Unknown error", e))
            }
        }
}
