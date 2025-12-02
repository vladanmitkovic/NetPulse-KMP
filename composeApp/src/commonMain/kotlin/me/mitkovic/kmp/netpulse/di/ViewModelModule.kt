package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.ui.AppViewModel
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module
class ViewModelModule {

    @Factory
    fun provideAppViewModel(
        appRepository: IAppRepository,
        logger: IAppLogger,
    ): AppViewModel =
        AppViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideHomeScreenViewModel(
        appRepository: IAppRepository,
        logger: IAppLogger,
    ): HomeScreenViewModel =
        HomeScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideSpeedTestScreenViewModel(
        appRepository: IAppRepository,
        logger: IAppLogger,
        @InjectedParam serverId: Int,
    ): SpeedTestScreenViewModel =
        SpeedTestScreenViewModel(
            appRepository = appRepository,
            logger = logger,
            serverId = serverId,
        )

    @Factory
    fun provideHistoryScreenViewModel(
        appRepository: IAppRepository,
        logger: IAppLogger,
    ): HistoryScreenViewModel =
        HistoryScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideSettingsScreenViewModel(
        appRepository: IAppRepository,
        logger: IAppLogger,
    ): SettingsScreenViewModel =
        SettingsScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )
}
