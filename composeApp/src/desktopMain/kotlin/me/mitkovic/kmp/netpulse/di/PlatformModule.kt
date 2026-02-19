package me.mitkovic.kmp.netpulse.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import me.mitkovic.kmp.netpulse.data.local.database.DatabaseFactory
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.installCommonPlugins
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<DatabaseFactory> {
            DatabaseFactory()
        }

        single<IThemeDataStorage> {
            ThemeDataStorageImpl()
        }

        single<ISettingsDataStorage> {
            SettingsDataStorageImpl()
        }

        single<HttpClient> {
            val xmlFormat: XML = get()
            HttpClient(CIO) {
                installCommonPlugins(xmlFormat)

                engine {
                    pipelining = true
                }
            }
        }
    }
