package me.mitkovic.kmp.netpulse.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.mitkovic.kmp.netpulse.data.local.ILocalStorage
import me.mitkovic.kmp.netpulse.data.local.LocalStorageImpl
import me.mitkovic.kmp.netpulse.data.local.database.DatabaseFactory
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
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

// Shared DB wrapper – platform only needs to provide SqlDriver.
@Module
class DatabaseModule {

    @Single
    fun provideNetPulseDatabase(
        @Provided factory: DatabaseFactory,
    ): NetPulseDatabase =
        factory.create()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
}

// Shared local storage wiring – platform provides IThemeDataStorage & ISettingsDataStorage.
@Module
class LocalStorageModule {

    @Single
    fun provideServerStorage(
        @Provided database: NetPulseDatabase,
    ): IServerStorage =
        ServerStorageImpl(
            database = database,
        )

    @Single
    fun provideTestResultStorage(
        @Provided database: NetPulseDatabase,
        @Provided logger: IAppLogger,
    ): ITestResultStorage =
        TestResultStorageImpl(
            database = database,
            logger = logger,
        )

    @Single
    fun provideLocationStorage(
        @Provided database: NetPulseDatabase,
    ): ILocationStorage =
        LocationStorageImpl(
            database = database,
        )

    @Single
    fun provideLocalStorage(
        testResultStorage: ITestResultStorage,
        serverStorage: IServerStorage,
        locationStorage: ILocationStorage,
        @Provided themeDataStorage: IThemeDataStorage,
        @Provided settingsDataStorage: ISettingsDataStorage,
    ): ILocalStorage =
        LocalStorageImpl(
            testResultStorage = testResultStorage,
            serverStorage = serverStorage,
            locationStorage = locationStorage,
            themeDataStorage = themeDataStorage,
            settingsDataStorage = settingsDataStorage,
        )
}

// Shared remote service wiring – platform only needs to provide HttpClient (engine) + XML.
@Module
class RemoteModule {

    @Single
    fun provideRemoteService(
        @Provided client: HttpClient,
        @Provided logger: IAppLogger,
        @Provided xml: XML,
    ): IRemoteService =
        RemoteServiceImpl(
            client = client,
            logger = logger,
            xmlFormat = xml,
        )
}
