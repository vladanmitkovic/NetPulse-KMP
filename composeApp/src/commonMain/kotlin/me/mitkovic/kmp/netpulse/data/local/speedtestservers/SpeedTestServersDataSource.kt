package me.mitkovic.kmp.netpulse.data.local.speedtestservers

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse

interface SpeedTestServersDataSource {

    suspend fun saveSpeedTestServers(response: SpeedTestServersResponse)

    fun getSpeedTestServers(): Flow<SpeedTestServersResponse?>

    suspend fun clearSpeedTestServers()
}
