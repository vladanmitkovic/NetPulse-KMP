package me.mitkovic.kmp.netpulse.data.local.testresult

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.local.database.TestSession

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
        ping: Double?,
        jitter: Double?,
        testTimestamp: Long,
    ): Long

    suspend fun updateTestSessionPingJitter(
        sessionId: Long,
        ping: Double,
        jitter: Double,
    )

    fun getLatestTestSession(): Flow<TestSession?>

    fun getLatestSessionByServerId(serverId: String): Flow<TestSession?>

    suspend fun insertTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    )

    fun getLatestTestResult(): Flow<TestResult?>

    fun getTestSessions(): Flow<List<TestSession>>

    fun getTestResultsBySessionId(sessionId: Long): Flow<List<TestResult>>
}
