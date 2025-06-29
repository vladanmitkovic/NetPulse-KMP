package me.mitkovic.kmp.netpulse.domain.model

data class SpeedTestResult(
    val timestamp: Long,
    val downloadSpeed: Double? = null,
    val uploadSpeed: Double? = null,
)
