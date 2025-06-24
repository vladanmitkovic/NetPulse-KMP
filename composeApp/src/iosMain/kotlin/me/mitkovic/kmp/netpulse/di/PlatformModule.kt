package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.xml.xml
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.local.LocalDataSourceImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSource
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSourceImpl
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
                name = "net_pulse.db",
            )
        }

        single {
            NetPulseDatabase(
                driver = get<NativeSqliteDriver>(),
            )
        }

        single<LocalDataSource> {
            LocalDataSourceImpl(
                database = get<NetPulseDatabase>(),
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
                }
            }
        }

        // RemoteDataSource binding
        single<RemoteDataSource> {
            RemoteDataSourceImpl(
                client = get<HttpClient>(),
                logger = get<AppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
