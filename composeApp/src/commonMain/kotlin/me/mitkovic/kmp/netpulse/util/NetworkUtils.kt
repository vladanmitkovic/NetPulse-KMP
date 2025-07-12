package me.mitkovic.kmp.netpulse.util

/**
 * Calculates the average latency from a list of latencies.
 * @param latencies List of latency values in milliseconds.
 * @return Average latency, or -1.0 if the list is empty.
 */
fun calculateAverage(latencies: List<Double>): Double = if (latencies.isEmpty()) -1.0 else latencies.sum() / latencies.size

/**
 * Calculates the average jitter from a list of latencies.
 * @param latencies List of latency values in milliseconds.
 * @return Average jitter, or 0.0 if insufficient data.
 */
fun calculateJitter(latencies: List<Double>): Double {
    if (latencies.size <= 1) return 0.0
    var totalJitter = 0.0
    for (i in 1 until latencies.size) {
        totalJitter += kotlin.math.abs(latencies[i] - latencies[i - 1])
    }
    return totalJitter / (latencies.size - 1)
}

/**
 * Calculates the packet loss percentage from total and failed requests.
 * @param totalRequests Total number of ping attempts.
 * @param failedRequests Number of failed ping attempts.
 * @return Packet loss percentage, or 0.0 if no requests.
 */
fun calculatePacketLoss(
    totalRequests: Int,
    failedRequests: Int,
): Double {
    if (totalRequests == 0) return 0.0
    return (failedRequests.toDouble() / totalRequests) * 100
}
