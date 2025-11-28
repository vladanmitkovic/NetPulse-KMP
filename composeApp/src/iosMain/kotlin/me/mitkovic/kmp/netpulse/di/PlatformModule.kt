package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
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
import platform.Foundation.NSUserDefaults

actual fun platformModule() =
    module {
        single<NSUserDefaults> {
            NSUserDefaults.standardUserDefaults()
        }

        single<SqlDriver> {
            NativeSqliteDriver(
                schema = NetPulseDatabase.Schema,
                name = "net_pulse_fifth.db",
            )
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
            ThemeDataStorageImpl(
                defaults = get<NSUserDefaults>(),
            )
        }

        single<ISettingsDataStorage> {
            SettingsDataStorageImpl(
                defaults = get<NSUserDefaults>(),
            )
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

        // Ktor client with Darwin engine + XML support
        single<HttpClient> {
            val xmlFormat: XML = get<XML>()
            HttpClient(Darwin) {
                installCommonPlugins(xmlFormat)

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
        single<IRemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<IAppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
