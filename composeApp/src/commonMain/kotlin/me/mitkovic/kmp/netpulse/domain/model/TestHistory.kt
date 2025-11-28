package me.mitkovic.kmp.netpulse.domain.model

data class TestHistory(
    val sessionId: Long,
    val timestamp: Long,
    val serverName: String,
    val serverCountry: String,
    val serverSponsor: String,
    val serverDistance: Double,
    val ping: Double?,
    val jitter: Double?,
    val packetLoss: Double?,
    val downloadSpeed: Double?,
    val uploadSpeed: Double?,
    val downloadSpeeds: List<Float>,
    val uploadSpeeds: List<Float>,
)
