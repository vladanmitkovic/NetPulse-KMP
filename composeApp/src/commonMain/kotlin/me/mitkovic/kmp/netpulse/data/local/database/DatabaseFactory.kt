package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<NetPulseDatabase>
}
