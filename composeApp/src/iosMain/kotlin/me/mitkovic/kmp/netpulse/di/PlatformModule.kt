package me.mitkovic.kmp.netpulse.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import me.mitkovic.kmp.netpulse.data.local.database.DatabaseFactory
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.installCommonPlugins
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() =
    module {
        single<NSUserDefaults> {
            NSUserDefaults.standardUserDefaults()
        }

        single<DatabaseFactory> {
            DatabaseFactory()
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
    }
