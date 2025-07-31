package me.mitkovic.kmp.netpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.domain.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.UserLocation

interface SpeedTestRepository {
    suspend fun fetchAndSaveUserLocation(): UserLocation?

    fun getServers(): Flow<Resource<ServersResponse?>>

    fun syncServers(): Flow<Resource<ServersResponse?>>

    suspend fun findLowestLatencyServer(): Server?

    suspend fun findClosestServerByDistance(): Server?

    suspend fun getServer(serverId: Int): Server?

    fun executeSpeedTest(server: Server): Flow<SpeedTestProgress>
}
