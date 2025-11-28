package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import me.mitkovic.kmp.netpulse.data.local.ILocalStorage
import me.mitkovic.kmp.netpulse.data.local.LocalStorageImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.location.ILocationStorage
import me.mitkovic.kmp.netpulse.data.local.location.LocationStorageImpl
import me.mitkovic.kmp.netpulse.data.local.server.IServerStorage
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorageImpl
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.testresult.ITestResultStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.IRemoteService
import me.mitkovic.kmp.netpulse.data.remote.RemoteServiceImpl
import me.mitkovic.kmp.netpulse.data.remote.installCommonPlugins
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module

actual fun platformModule() =
    module {
        /*
        single {
            JdbcSqliteDriver("jdbc:sqlite:net_pulse.db").apply {
                NetPulseDatabase.Schema.create(this)
            }
        }
         */

        single<SqlDriver> {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                NetPulseDatabase.Schema.create(this)
            }
        }

        single {
            NetPulseDatabase(
                driver = get<SqlDriver>(),
            )
        }

        single<IServerStorage> {
            ServerStorageImpl(database = get<NetPulseDatabase>())
        }

        single<ITestResultStorage> {
            TestResultStorageImpl(
                database = get<NetPulseDatabase>(),
                logger = get<IAppLogger>(),
            )
        }

        single<ILocationStorage> {
            LocationStorageImpl(database = get<NetPulseDatabase>())
        }

        single<IThemeDataStorage> {
            ThemeDataStorageImpl()
        }

        single<ISettingsDataStorage> {
            SettingsDataStorageImpl()
        }

        single<ILocalStorage> {
            LocalStorageImpl(
                testResultStorage = get<ITestResultStorage>(),
                serverStorage = get<IServerStorage>(),
                locationStorage = get<ILocationStorage>(),
                themeDataStorage = get<IThemeDataStorage>(),
                settingsDataStorage = get<ISettingsDataStorage>(),
            )
        }

        // Ktor client with CIO engine + XML support
        single<HttpClient> {
            val xmlFormat: XML = get<XML>()
            HttpClient(CIO) {
                installCommonPlugins(xmlFormat)

                engine {
                    pipelining = true
                }
            }
        }

        // RemoteDataSource binding
        single<IRemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<IAppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
