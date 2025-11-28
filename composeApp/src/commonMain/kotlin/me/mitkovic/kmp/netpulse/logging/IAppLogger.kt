package me.mitkovic.kmp.netpulse.logging

interface IAppLogger {

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
