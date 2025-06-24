// androidMain/src/me/mitkovic/kmp/netpulse/di/PlatformModule.kt
package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.xml.xml
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.local.LocalDataSourceImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSource
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSourceImpl
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.logging.AppLoggerImpl
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AppLogger> { AppLoggerImpl() }

        single<SqlDriver> {
            AndroidSqliteDriver(
                NetPulseDatabase.Schema,
                context = androidContext(),
                name = "net_pulse.db",
            )
        }

        single<NetPulseDatabase> {
            NetPulseDatabase(
                driver = get<SqlDriver>(),
            )
        }

        single<LocalDataSource> {
            LocalDataSourceImpl(
                database = get<NetPulseDatabase>(),
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
                }
            }
        }

        single<RemoteDataSource> {
            RemoteDataSourceImpl(
                client = get<HttpClient>(),
                logger = get<AppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
