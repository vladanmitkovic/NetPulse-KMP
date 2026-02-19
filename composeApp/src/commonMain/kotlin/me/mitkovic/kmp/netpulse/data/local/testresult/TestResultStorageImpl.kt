package me.mitkovic.kmp.netpulse.data.local.testresult

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.local.database.TestResultEntity
import me.mitkovic.kmp.netpulse.data.local.database.TestSession
import me.mitkovic.kmp.netpulse.data.local.database.TestSessionEntity
import me.mitkovic.kmp.netpulse.logging.IAppLogger

class TestResultStorageImpl(
    private val database: NetPulseDatabase,
    private val logger: IAppLogger,
) : ITestResultStorage {

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
        packetLoss: Double?,
        testTimestamp: Long,
    ): Long =
        database.testResultDao().insertTestSession(
            TestSessionEntity(
                serverId = serverId,
                serverUrl = serverUrl,
                serverName = serverName,
                serverCountry = serverCountry,
                serverSponsor = serverSponsor,
                serverHost = serverHost,
                serverDistance = serverDistance,
                ping = ping,
                jitter = jitter,
                packetLoss = packetLoss,
                testTimestamp = testTimestamp,
                testLocationId = testLocationId,
            ),
        )

    override fun getLatestTestSession(): Flow<TestSession?> =
        database.testResultDao()
            .observeLatestTestSession()
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
                        packetLoss = it.packetLoss,
                    )
                }
            }

    override fun getLatestSessionByServerId(serverId: String): Flow<TestSession?> =
        database.testResultDao()
            .observeLatestSessionByServerId(serverId)
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
                        packetLoss = it.packetLoss,
                    )
                }
            }

    override suspend fun updateTestSessionPingJitterPacketLoss(
        sessionId: Long,
        ping: Double,
        jitter: Double,
        packetLoss: Double,
    ) {
        database.testResultDao().updateTestSessionPingJitterPacketLoss(
            sessionId = sessionId,
            ping = ping,
            jitter = jitter,
            packetLoss = packetLoss,
        )
    }

    override suspend fun insertTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    ) {
        database.testResultDao().insertTestResult(
            TestResultEntity(
                sessionId = sessionId,
                testType = testType.toLong(),
                speed = speed,
                resultTimestamp = resultTimestamp,
            ),
        )
        logger.logDebug("TestResultStorageImpl", "Triggering insert for sessionId=$sessionId, testType=$testType")
    }

    override fun getLatestTestResult(): Flow<TestResult?> =
        database.testResultDao()
            .observeLatestTestResult()
            .map { entity ->
                entity?.let {
                    TestResult(
                        resultId = it.resultId,
                        sessionId = it.sessionId,
                        testType = it.testType,
                        speed = it.speed,
                        resultTimestamp = it.resultTimestamp,
                    )
                }
            }

    override fun getTestSessions(): Flow<List<TestSession>> =
        database.testResultDao()
            .observeTestSessions()
            .map { entities ->
                entities.map { entity ->
                    TestSession(
                        sessionId = entity.sessionId,
                        serverId = entity.serverId,
                        serverUrl = entity.serverUrl,
                        serverName = entity.serverName,
                        serverCountry = entity.serverCountry,
                        serverSponsor = entity.serverSponsor,
                        serverHost = entity.serverHost,
                        serverDistance = entity.serverDistance,
                        ping = entity.ping,
                        jitter = entity.jitter,
                        packetLoss = entity.packetLoss,
                        testTimestamp = entity.testTimestamp,
                        testLocationId = entity.testLocationId,
                    )
                }
            }

    override fun getTestResultsBySessionId(sessionId: Long): Flow<List<TestResult>> =
        database.testResultDao()
            .observeTestResultsBySessionId(sessionId)
            .map { entities ->
                entities.map { entity ->
                    TestResult(
                        resultId = entity.resultId,
                        sessionId = entity.sessionId,
                        testType = entity.testType,
                        speed = entity.speed,
                        resultTimestamp = entity.resultTimestamp,
                    )
                }
            }

    override suspend fun deleteTestSession(sessionId: Long) {
        database.testResultDao().deleteSessionAndResults(sessionId)
        logger.logDebug("TestResultStorageImpl", "Deleted sessionId=$sessionId and its results")
    }
}
