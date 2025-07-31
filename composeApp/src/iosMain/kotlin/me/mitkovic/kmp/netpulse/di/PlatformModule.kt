package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
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
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module

actual fun platformModule() =
    module {
        // Logger
        single<AppLogger> { AppLoggerImpl() }

        single {
            NativeSqliteDriver(
                schema = NetPulseDatabase.Schema,
                name = "net_pulse_fourth.db",
            )
        }

        single {
            NetPulseDatabase(
                driver = get<NativeSqliteDriver>(),
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

        // xmlutil formatter (default settings fine)
        single<XML> {
            XML {
                indentString = "  "
                repairNamespaces = true
            }
        }

        // Ktor client with Darwin engine + XML support
        single<HttpClient> {
            val xmlFormat: XML = get<XML>()
            HttpClient(Darwin) {
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
                    configureSession {
                        allowsCellularAccess = true
                        timeoutIntervalForRequest = 15.0
                        timeoutIntervalForResource = 30.0
                    }
                }
            }
        }

        // RemoteDataSource binding
        single<RemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<AppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
