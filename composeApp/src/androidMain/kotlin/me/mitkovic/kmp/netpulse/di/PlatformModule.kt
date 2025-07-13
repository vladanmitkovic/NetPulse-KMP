package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.serialization.json.Json
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.local.LocalStorageImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.location.LocationStorage
import me.mitkovic.kmp.netpulse.data.local.location.LocationStorageImpl
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorage
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorageImpl
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.RemoteService
import me.mitkovic.kmp.netpulse.data.remote.RemoteServiceImpl
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.logging.AppLoggerImpl
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.ConnectionPool
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

actual fun platformModule() =
    module {
        single<AppLogger> { AppLoggerImpl() }

        single<SqlDriver> {
            val context = androidContext()

            // Delete the corrupted database
            context.deleteDatabase("net_pulse.db")

            val driver =
                AndroidSqliteDriver(
                    schema = NetPulseDatabase.Schema,
                    context = androidContext(),
                    name = "net_pulse_third.db",
                )
            driver
        }

        single<NetPulseDatabase> {
            NetPulseDatabase(
                driver = get<SqlDriver>(),
            )
        }

        single<ServerStorage> {
            ServerStorageImpl(database = get<NetPulseDatabase>())
        }

        single<TestResultStorage> {
            TestResultStorageImpl(
                database = get<NetPulseDatabase>(),
                logger = get<AppLogger>(),
            )
        }

        single<LocationStorage> {
            LocationStorageImpl(database = get<NetPulseDatabase>())
        }

        single<LocalStorage> {
            LocalStorageImpl(
                testResultStorage = get<TestResultStorage>(),
                serverStorage = get<ServerStorage>(),
                locationStorage = get<LocationStorage>(),
            )
        }

        single<XML> {
            XML {
                xmlVersion = XmlVersion.XML10
                xmlDeclMode = XmlDeclMode.Auto
                indentString = "  "
                repairNamespaces = true
            }
        }

        single<HttpClient> {
            val xmlFormat: XML = get<XML>()
            HttpClient(OkHttp) {
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
                engine {
                    config {
                        connectionPool(ConnectionPool(maxIdleConnections = 50, keepAliveDuration = 15, TimeUnit.SECONDS))
                        connectTimeout(10, TimeUnit.SECONDS)
                        readTimeout(15, TimeUnit.SECONDS)
                        writeTimeout(15, TimeUnit.SECONDS)
                    }
                }
            }
        }

        single<RemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<AppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
