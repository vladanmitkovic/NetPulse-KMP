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
import org.koin.core.annotation.Provided

@Module
class ViewModelModule {

    @Factory
    fun provideAppViewModel(
        @Provided appRepository: IAppRepository,
        @Provided logger: IAppLogger,
    ): AppViewModel =
        AppViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideHomeScreenViewModel(
        @Provided appRepository: IAppRepository,
        @Provided logger: IAppLogger,
    ): HomeScreenViewModel =
        HomeScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideSpeedTestScreenViewModel(
        @Provided appRepository: IAppRepository,
        @Provided logger: IAppLogger,
        @InjectedParam serverId: Int,
    ): SpeedTestScreenViewModel =
        SpeedTestScreenViewModel(
            appRepository = appRepository,
            logger = logger,
            serverId = serverId,
        )

    @Factory
    fun provideHistoryScreenViewModel(
        @Provided appRepository: IAppRepository,
        @Provided logger: IAppLogger,
    ): HistoryScreenViewModel =
        HistoryScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )

    @Factory
    fun provideSettingsScreenViewModel(
        @Provided appRepository: IAppRepository,
        @Provided logger: IAppLogger,
    ): SettingsScreenViewModel =
        SettingsScreenViewModel(
            appRepository = appRepository,
            logger = logger,
        )
}
