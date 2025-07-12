package me.mitkovic.kmp.netpulse.data.local.testresult

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
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
        testTimestamp: Long,
    ): Long {
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.insertTestSession(
                serverId = serverId,
                serverUrl = serverUrl,
                serverName = serverName,
                serverCountry = serverCountry,
                serverSponsor = serverSponsor,
                serverHost = serverHost,
                testTimestamp = testTimestamp,
            )
        }
        return database.netPulseDatabaseQueries.lastInsertRowId().executeAsOne()
    }

    private val resultInsertedTrigger = Channel<Unit>(capacity = Channel.UNLIMITED)

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
        logger.logDebug("TestResultDataSourceImpl", "Triggering insert for sessionId=$sessionId, testType=$testType")
        resultInsertedTrigger.trySend(Unit)
    }

    override fun getLatestTestResult(): Flow<TestResult?> =
        resultInsertedTrigger
            .receiveAsFlow()
            .onStart { emit(Unit) }
            .map {
                database.netPulseDatabaseQueries
                    .getLatestTestResult()
                    .executeAsOneOrNull()
            }
}
