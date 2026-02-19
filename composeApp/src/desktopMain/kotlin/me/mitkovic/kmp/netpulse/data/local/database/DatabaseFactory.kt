package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<NetPulseDatabase> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "net_pulse.db")
        return Room.databaseBuilder<NetPulseDatabase>(
            name = dbFile.absolutePath,
        )
    }
}
