package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.local.ILocalStorage
import me.mitkovic.kmp.netpulse.data.remote.IRemoteService
import me.mitkovic.kmp.netpulse.data.repository.AppRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.data.repository.settings.ISettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.speedtest.SpeedTestRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.theme.IThemeRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.ThemeRepositoryImpl
import me.mitkovic.kmp.netpulse.domain.repository.ISpeedTestRepository
import me.mitkovic.kmp.netpulse.logging.AppLoggerImpl
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.platform.Platform
import me.mitkovic.kmp.netpulse.platform.getPlatform
import nl.adaptivity.xmlutil.serialization.XML
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
class AppModule {

    @Single
    fun providePlatform(): Platform = getPlatform()

    @Single
    fun provideAppLogger(): IAppLogger = AppLoggerImpl()

    // Default XML config (can be overridden in platform modules)
    @Single
    fun provideXml(): XML =
        XML {
            indentString = "  "
            repairNamespaces = true
        }

    @Single
    fun provideThemeRepository(
        @Provided localStorage: ILocalStorage,
    ): IThemeRepository =
        ThemeRepositoryImpl(
            localStorage = localStorage,
        )

    @Single
    fun provideSettingsRepository(
        @Provided localStorage: ILocalStorage,
    ): ISettingsRepository =
        SettingsRepositoryImpl(
            localStorage = localStorage,
        )

    @Single
    fun provideSpeedTestRepository(
        @Provided localStorage: ILocalStorage,
        @Provided remoteService: IRemoteService,
        settingsRepository: ISettingsRepository,
        logger: IAppLogger,
    ): ISpeedTestRepository =
        SpeedTestRepositoryImpl(
            localStorage = localStorage,
            remoteService = remoteService,
            settingsRepository = settingsRepository,
            logger = logger,
        )

    @Single
    fun provideAppRepository(
        speedTestRepository: ISpeedTestRepository,
        themeRepository: IThemeRepository,
        settingsRepository: ISettingsRepository,
    ): IAppRepository =
        AppRepositoryImpl(
            speedTestRepository = speedTestRepository,
            themeRepository = themeRepository,
            settingsRepository = settingsRepository,
        )
}
