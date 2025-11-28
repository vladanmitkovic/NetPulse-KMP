package me.mitkovic.kmp.netpulse.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML

// Shared Ktor plugin setup for all platforms.
// Platform HttpClients call installCommonPlugins(xmlFormat) and keep only engine-specific config.
fun HttpClientConfig<*>.installCommonPlugins(xmlFormat: XML) {
    install(DefaultRequest) {
        header(HttpHeaders.UserAgent, "Mozilla/5.0 (compatible; NetPulse/1.0)")
    }
    install(ContentNegotiation) {
        xml(xmlFormat)
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 10_000
        requestTimeoutMillis = 15_000
        socketTimeoutMillis = 15_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
}
