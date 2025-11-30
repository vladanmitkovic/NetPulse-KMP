package me.mitkovic.kmp.netpulse.di

import app.cash.sqldelight.db.SqlDriver
import io.ktor.client.HttpClient
import me.mitkovic.kmp.netpulse.data.local.ILocalStorage
import me.mitkovic.kmp.netpulse.data.local.LocalStorageImpl
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.location.ILocationStorage
import me.mitkovic.kmp.netpulse.data.local.location.LocationStorageImpl
import me.mitkovic.kmp.netpulse.data.local.server.IServerStorage
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorageImpl
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.ITestResultStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorageImpl
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage
import me.mitkovic.kmp.netpulse.data.remote.IRemoteService
import me.mitkovic.kmp.netpulse.data.remote.RemoteServiceImpl
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.dsl.module

// Shared DB wrapper – platform only needs to provide SqlDriver.
val databaseModule =
    module {
        single<NetPulseDatabase> {
            NetPulseDatabase(
                driver = get<SqlDriver>(),
            )
        }
    }

// Shared local storage wiring – platform provides IThemeDataStorage & ISettingsDataStorage.
val localStorageModule =
    module {
        single<IServerStorage> {
            ServerStorageImpl(
                database = get<NetPulseDatabase>(),
            )
        }

        single<ITestResultStorage> {
            TestResultStorageImpl(
                database = get<NetPulseDatabase>(),
                logger = get<IAppLogger>(),
            )
        }

        single<ILocationStorage> {
            LocationStorageImpl(
                database = get<NetPulseDatabase>(),
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
    }

// Shared remote service wiring – platform only needs to provide HttpClient (engine) + XML if desired.
val remoteModule =
    module {
        single<IRemoteService> {
            RemoteServiceImpl(
                client = get<HttpClient>(),
                logger = get<IAppLogger>(),
                xmlFormat = get<XML>(),
            )
        }
    }
