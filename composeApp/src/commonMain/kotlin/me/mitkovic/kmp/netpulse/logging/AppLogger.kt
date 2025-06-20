package me.mitkovic.kmp.netpulse.logging

interface AppLogger {

    fun logDebug(
        tag: String? = null,
        message: String,
    )

    fun logError(
        tag: String? = null,
        message: String?,
        throwable: Throwable?,
    )
}
