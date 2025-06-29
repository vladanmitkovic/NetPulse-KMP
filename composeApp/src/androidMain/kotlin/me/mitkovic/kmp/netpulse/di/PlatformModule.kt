package me.mitkovic.kmp.netpulse.di

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AppLogger> { AppLoggerImpl() }

        single<SqlDriver> {
            // val context = androidContext()

            // Delete the corrupted database
            // context.deleteDatabase("net_pulse.db")

            val driver =
                AndroidSqliteDriver(
                    schema = NetPulseDatabase.Schema,
                    context = androidContext(),
                    name = "net_pulse.db",
                    callback =
                        object : AndroidSqliteDriver.Callback(NetPulseDatabase.Schema) {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                Log.d("DatabaseInit", "Database opened successfully")
                                db.setForeignKeyConstraintsEnabled(true)
                            }

                            override fun onCreate(db: SupportSQLiteDatabase) {
                                Log.d("DatabaseInit", "Database created successfully")
                                super.onCreate(db)
                            }

                            override fun onUpgrade(
                                db: SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int,
                            ) {
                                Log.d("DatabaseInit", "Database upgraded from $oldVersion to $newVersion")
                                super.onUpgrade(db, oldVersion, newVersion)
                            }
                        },
                )

            // Test if schema creation worked
            try {
                driver.executeQuery(
                    identifier = null,
                    sql = "SELECT name FROM sqlite_master WHERE type='table'",
                    mapper = { cursor ->
                        val tables = mutableListOf<String>()
                        while (cursor.next().value) {
                            tables.add(cursor.getString(0) ?: "")
                        }
                        Log.d("DatabaseInit", "Tables found: $tables")
                        QueryResult.Value(tables)
                    },
                    parameters = 0,
                ) {}
            } catch (e: Exception) {
                Log.e("DatabaseInit", "Error checking tables", e)
            }

            driver
        }

        single<NetPulseDatabase> {
            NetPulseDatabase(
                driver = get<SqlDriver>(),
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
