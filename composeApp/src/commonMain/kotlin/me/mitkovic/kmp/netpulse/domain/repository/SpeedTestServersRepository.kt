package me.mitkovic.kmp.netpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.database.SpeedTestResultEntity
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.domain.model.SpeedTestServersResponse

interface SpeedTestServersRepository {
    fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>>

    fun refreshSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>>

    suspend fun findNearestServer(): Server?

    suspend fun getSpeedTestServer(serverId: Int): Server?

    suspend fun runSpeedTest(server: Server)

    fun observeSpeedTestResults(): Flow<Resource<SpeedTestResultEntity?>>
}
