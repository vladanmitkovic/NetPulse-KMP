package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TestResult",
    foreignKeys = [
        ForeignKey(
            entity = TestSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index("sessionId")]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val sessionId: Long,
    val testType: Long,
    val speed: Double?,
    val resultTimestamp: Long,
)
