package me.mitkovic.kmp.netpulse.data.model

data class SpeedTestProgress(
    val ping: Double? = null,
    val jitter: Double? = null,
    val downloadSpeed: Double? = null,
    val uploadSpeed: Double? = null,
    val isCompleted: Boolean = false,
    val error: String? = null,
)
