package me.mitkovic.kmp.netpulse.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.xml.xml
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
