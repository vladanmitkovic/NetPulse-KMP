package me.mitkovic.kmp.netpulse.data.local.speedtestresults

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.database.SpeedTestResultEntity

interface SpeedTestResultsDataSource {

    suspend fun insertSpeedTestSession(
        serverId: String,
        serverUrl: String,
        serverName: String,
        serverCountry: String,
        serverSponsor: String,
        serverHost: String,
        testTimestamp: Long,
    ): Long

    suspend fun insertSpeedTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    )

    fun getLatestSpeedTestResult(): Flow<SpeedTestResultEntity?>
}
