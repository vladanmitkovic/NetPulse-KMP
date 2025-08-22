package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
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

        /*
        single {
            JdbcSqliteDriver("jdbc:sqlite:net_pulse.db").apply {
                NetPulseDatabase.Schema.create(this)
            }
        }
         */

        single<JdbcSqliteDriver> {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                NetPulseDatabase.Schema.create(this)
            }
        }

        single {
            NetPulseDatabase(
                driver = get<JdbcSqliteDriver>(),
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

        single<ThemeDataStorage> {
            ThemeDataStorageImpl()
        }

        single<SettingsDataStorage> {
            SettingsDataStorageImpl()
        }

        single<LocalStorage> {
            LocalStorageImpl(
                testResultStorage = get<TestResultStorage>(),
                serverStorage = get<ServerStorage>(),
                locationStorage = get<LocationStorage>(),
                themeDataStorage = get<ThemeDataStorage>(),
                settingsDataStorage = get<SettingsDataStorage>(),
            )
        }

        // xmlutil formatter
        single<XML> {
            XML {
                indentString = "  "
                repairNamespaces = true
            }
        }

        // Ktor client with CIO engine + XML support
        single<HttpClient> {
            val xmlFormat: XML = get<XML>()
            HttpClient(CIO) {
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
                    pipelining = true
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
