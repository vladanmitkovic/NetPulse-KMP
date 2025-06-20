package me.mitkovic.kmp.netpulse.logging

class AppLoggerImpl : AppLogger {

    override fun logDebug(
        tag: String?,
        message: String,
    ) {
        println("${tag ?: "Debug"}: $message")
    }

    override fun logError(
        tag: String?,
        message: String?,
        throwable: Throwable?,
    ) {
        println("${tag ?: "Error"}: $message")
        throwable?.printStackTrace()
    }
}
