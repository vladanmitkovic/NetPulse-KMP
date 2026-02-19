package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Server")
data class ServerEntity(
    @PrimaryKey val id: String,
    val url: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val country: String,
    val cc: String,
    val sponsor: String,
    val host: String,
    val distance: Double,
)
