package me.mitkovic.kmp.netpulse.data.local.database

data class TestResult(
    val resultId: Long,
    val sessionId: Long,
    val testType: Long,
    val speed: Double?,
    val resultTimestamp: Long,
)
