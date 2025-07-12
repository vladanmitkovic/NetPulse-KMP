package me.mitkovic.kmp.netpulse.data.model

/**
 * Data class to hold ping results: average latency, jitter, packet loss, and failed request count.
 */
data class PingResult(
    val averageLatency: Double,
    val jitter: Double,
    val packetLoss: Double,
    val failedRequests: Int,
)
