package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {

    @Insert
    suspend fun insertTestSession(entity: TestSessionEntity): Long

    @Query("SELECT * FROM TestSession ORDER BY testTimestamp DESC LIMIT 1")
    fun observeLatestTestSession(): Flow<TestSessionEntity?>

    @Query("SELECT * FROM TestSession WHERE serverId = :serverId ORDER BY testTimestamp DESC LIMIT 1")
    fun observeLatestSessionByServerId(serverId: String): Flow<TestSessionEntity?>

    @Query("UPDATE TestSession SET ping = :ping, jitter = :jitter, packetLoss = :packetLoss WHERE sessionId = :sessionId")
    suspend fun updateTestSessionPingJitterPacketLoss(sessionId: Long, ping: Double, jitter: Double, packetLoss: Double)

    @Insert
    suspend fun insertTestResult(entity: TestResultEntity): Long

    @Query("SELECT * FROM TestResult ORDER BY resultTimestamp DESC LIMIT 1")
    fun observeLatestTestResult(): Flow<TestResultEntity?>

    @Query("SELECT * FROM TestSession ORDER BY testTimestamp DESC")
    fun observeTestSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM TestResult WHERE sessionId = :sessionId ORDER BY resultTimestamp")
    fun observeTestResultsBySessionId(sessionId: Long): Flow<List<TestResultEntity>>

    @Query("DELETE FROM TestResult WHERE sessionId = :sessionId")
    suspend fun deleteTestResultsBySessionId(sessionId: Long)

    @Query("DELETE FROM TestSession WHERE sessionId = :sessionId")
    suspend fun deleteTestSession(sessionId: Long)

    @Transaction
    suspend fun deleteSessionAndResults(sessionId: Long) {
        deleteTestResultsBySessionId(sessionId)
        deleteTestSession(sessionId)
    }
}
