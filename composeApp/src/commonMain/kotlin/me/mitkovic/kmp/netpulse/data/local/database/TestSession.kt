package me.mitkovic.kmp.netpulse.data.local.database

data class TestSession(
    val sessionId: Long,
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
