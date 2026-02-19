package me.mitkovic.kmp.netpulse.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context,
) {
    actual fun create(): RoomDatabase.Builder<NetPulseDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("net_pulse.db")
        return Room.databaseBuilder<NetPulseDatabase>(
            context = appContext,
            name = dbFile.absolutePath,
        )
    }
}
