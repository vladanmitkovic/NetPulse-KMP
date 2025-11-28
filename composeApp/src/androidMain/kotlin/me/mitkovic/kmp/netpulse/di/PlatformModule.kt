package me.mitkovic.kmp.netpulse.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.ConnectionPool
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

actual fun platformModule() =
    module {
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create(
                produceFile = { get<Application>().preferencesDataStoreFile("user_preferences") },
            )
        }

        single<SqlDriver> {
            val context = androidContext()

            // Delete the corrupted database
            context.deleteDatabase("net_pulse_fourth.db")

            val driver =
                AndroidSqliteDriver(
                    schema = NetPulseDatabase.Schema,
                    context = androidContext(),
                    name = "net_pulse_fifth.db",
                )
            driver
        }

        single<NetPulseDatabase> {
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
                dataStore = get<DataStore<Preferences>>(),
            )
        }

        single<ISettingsDataStorage> {
            SettingsDataStorageImpl(
                dataStore = get<DataStore<Preferences>>(),
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
                installCommonPlugins(xmlFormat)

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

        single<IRemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<IAppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
