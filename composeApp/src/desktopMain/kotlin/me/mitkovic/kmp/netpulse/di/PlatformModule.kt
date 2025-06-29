package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.xml.xml
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.local.LocalDataSourceImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.speedtestresults.SpeedTestResultsDataSource
import me.mitkovic.kmp.netpulse.data.local.speedtestresults.SpeedTestResultsDataSourceImpl
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSource
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSourceImpl
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

        single<SpeedTestServersDataSource> {
            SpeedTestServersDataSourceImpl(database = get<NetPulseDatabase>())
        }

        single<SpeedTestResultsDataSource> {
            SpeedTestResultsDataSourceImpl(
                database = get<NetPulseDatabase>(),
                logger = get<AppLogger>(),
            )
        }

        single<LocalDataSource> {
            LocalDataSourceImpl(
                speedTestResults = get<SpeedTestResultsDataSource>(),
                speedTestServers = get<SpeedTestServersDataSource>(),
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
