package me.mitkovic.kmp.netpulse.data.local.speedtestresults

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.database.SpeedTestResultEntity
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SpeedTestResultsDataSourceImpl(
    private val database: NetPulseDatabase,
    private val logger: AppLogger,
) : SpeedTestResultsDataSource {

    override suspend fun insertSpeedTestSession(
        serverId: String,
        serverUrl: String,
        serverName: String,
        serverCountry: String,
        serverSponsor: String,
        serverHost: String,
        testTimestamp: Long,
    ): Long {
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.insertSpeedTestSession(
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

    override suspend fun insertSpeedTestResult(
        sessionId: Long,
        testType: Int,
        speed: Double,
        resultTimestamp: Long,
    ) {
        database.netPulseDatabaseQueries.insertSpeedTestResult(
            sessionId = sessionId,
            testType = testType.toLong(),
            speed = speed,
            resultTimestamp = resultTimestamp,
        )
        logger.logDebug("SpeedTestResultsDataSourceImpl", "Triggering insert for sessionId=$sessionId, testType=$testType")
        resultInsertedTrigger.trySend(Unit)
    }

    override fun getLatestSpeedTestResult(): Flow<SpeedTestResultEntity?> =
        resultInsertedTrigger
            .receiveAsFlow()
            .onStart { emit(Unit) }
            .map {
                val entity =
                    database.netPulseDatabaseQueries
                        .getLatestSpeedTestResult()
                        .executeAsOneOrNull()
                val result =
                    entity?.let {
                        SpeedTestResultEntity(
                            resultId = it.resultId,
                            sessionId = it.sessionId,
                            testType = it.testType,
                            speed = it.speed,
                            resultTimestamp = it.resultTimestamp,
                        )
                    }
                // logger.logDebug("SpeedTestResultsDataSourceImpl", "Emitting result: $result")
                result
            }
}
