package me.mitkovic.kmp.netpulse.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import me.mitkovic.kmp.netpulse.data.local.database.DatabaseFactory
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.installCommonPlugins
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
                produceFile = {
                    get<Application>().preferencesDataStoreFile("user_preferences")
                },
            )
        }

        single<DatabaseFactory> {
            DatabaseFactory(
                context = androidContext(),
            )
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

        // Android-specific XML config overrides common one if needed
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
                        connectionPool(
                            ConnectionPool(
                                50,
                                15,
                                TimeUnit.SECONDS,
                            ),
                        )
                        connectTimeout(10, TimeUnit.SECONDS)
                        readTimeout(15, TimeUnit.SECONDS)
                        writeTimeout(15, TimeUnit.SECONDS)
                    }
                }
            }
        }
    }
