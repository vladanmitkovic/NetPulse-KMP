package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorageImpl
import me.mitkovic.kmp.netpulse.data.remote.installCommonPlugins
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
            // In-memory DB for desktop (as you had)
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                NetPulseDatabase.Schema.create(this)
            }
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
