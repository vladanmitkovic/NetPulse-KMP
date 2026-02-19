package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "TestSession",
    foreignKeys = [
        ForeignKey(
            entity = TestLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["testLocationId"],
            onDelete = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index("testLocationId"), Index("serverId")]
)
data class TestSessionEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val serverId: String,
    val serverUrl: String,
    val serverName: String,
    val serverCountry: String,
    val serverSponsor: String,
    val serverHost: String,
    val serverDistance: Double,
    val ping: Double?,
    val jitter: Double?,
    val packetLoss: Double?,
    val testTimestamp: Long,
    val testLocationId: Long,
)
