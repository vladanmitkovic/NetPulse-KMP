package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        CurrentLocationEntity::class,
        TestLocationEntity::class,
        ServerEntity::class,
        TestSessionEntity::class,
        TestResultEntity::class,
    ],
    version = 1,
)
@ConstructedBy(NetPulseDatabaseConstructor::class)
abstract class NetPulseDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun serverDao(): ServerDao
    abstract fun testResultDao(): TestResultDao
}

@Suppress("KotlinNoActualForExpect")
expect object NetPulseDatabaseConstructor :
    RoomDatabaseConstructor<NetPulseDatabase> {
    override fun initialize(): NetPulseDatabase
}
