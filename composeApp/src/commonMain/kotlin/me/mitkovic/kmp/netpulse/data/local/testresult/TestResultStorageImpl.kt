package me.mitkovic.kmp.netpulse.data.local.testresult

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.local.database.TestSession
import me.mitkovic.kmp.netpulse.logging.AppLogger

class TestResultStorageImpl(
    private val database: NetPulseDatabase,
    private val logger: AppLogger,
) : TestResultStorage {

    override suspend fun insertTestSession(
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
    ): Long {
        var id: Long = 0
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.insertTestSession(
                serverId = serverId,
                serverUrl = serverUrl,
                serverName = serverName,
                serverCountry = serverCountry,
                serverSponsor = serverSponsor,
                serverHost = serverHost,
                serverDistance = serverDistance,
                testLocationId = testLocationId,
                ping = ping,
                jitter = jitter,
                testTimestamp = testTimestamp,
            )
            id = database.netPulseDatabaseQueries.lastInsertRowId().executeAsOne()
        }
        return id
    }

    override fun getLatestTestSession(): Flow<TestSession?> =
        database.netPulseDatabaseQueries
            .getLatestTestSession()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let {
                    TestSession(
                        sessionId = it.sessionId,
                        serverId = it.serverId,
                        serverUrl = it.serverUrl,
                        serverName = it.serverName,
                        serverCountry = it.serverCountry,
                        serverSponsor = it.serverSponsor,
                        serverHost = it.serverHost,
                        serverDistance = it.serverDistance,
                        testTimestamp = it.testTimestamp,
                        testLocationId = it.testLocationId,
                        ping = it.ping,
                        jitter = it.jitter,
                    )
                }
            }

    override fun getLatestSessionByServerId(serverId: String): Flow<TestSession?> =
        database.netPulseDatabaseQueries
            .getTestSessionByServerId(serverId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let {
                    TestSession(
                        sessionId = it.sessionId,
                        serverId = it.serverId,
                        serverUrl = it.serverUrl,
                        serverName = it.serverName,
                        serverCountry = it.serverCountry,
                        serverSponsor = it.serverSponsor,
                        serverHost = it.serverHost,
                        serverDistance = it.serverDistance,
                        testTimestamp = it.testTimestamp,
                        testLocationId = it.testLocationId,
                        ping = it.ping,
                        jitter = it.jitter,
                    )
                }
            }

    override suspend fun updateTestSessionPingJitter(
        sessionId: Long,
        ping: Double,
        jitter: Double,
    ) {
        database.netPulseDatabaseQueries.updateTestSessionPingJitter(ping, jitter, sessionId)
    }

    override suspend fun insertTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    ) {
        database.netPulseDatabaseQueries.insertTestResult(
            sessionId = sessionId,
            testType = testType.toLong(),
            speed = speed,
            resultTimestamp = resultTimestamp,
        )
        logger.logDebug("TestResultStorageImpl", "Triggering insert for sessionId=$sessionId, testType=$testType")
    }

    override fun getLatestTestResult(): Flow<TestResult?> =
        database.netPulseDatabaseQueries
            .getLatestTestResult()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
}
