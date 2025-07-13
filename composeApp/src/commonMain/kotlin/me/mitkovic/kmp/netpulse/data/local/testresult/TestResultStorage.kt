package me.mitkovic.kmp.netpulse.data.local.testresult

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.database.TestResult

interface TestResultStorage {
    suspend fun insertTestSession(
        serverId: String,
        serverUrl: String,
        serverName: String,
        serverCountry: String,
        serverSponsor: String,
        serverHost: String,
        serverDistance: Double,
        testLocationId: Long,
        testTimestamp: Long,
    ): Long

    suspend fun insertTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    )

    fun getLatestTestResult(): Flow<TestResult?>
}
