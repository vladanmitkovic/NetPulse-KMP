package me.mitkovic.kmp.netpulse.data.remote

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse
import me.mitkovic.kmp.netpulse.domain.model.Server

interface RemoteDataSource {

    suspend fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse>>

    suspend fun findNearestServer(servers: List<Server>): Server?

    suspend fun runSpeedTest(
        server: Server,
        localDataSource: LocalDataSource,
    )
}
