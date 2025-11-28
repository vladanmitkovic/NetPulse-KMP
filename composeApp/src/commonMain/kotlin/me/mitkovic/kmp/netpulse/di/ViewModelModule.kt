package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.ui.AppViewModel
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        factory {
            AppViewModel(
                appRepository = get<IAppRepository>(),
                logger = get<IAppLogger>(),
            )
        }
        factory {
            HomeScreenViewModel(
                appRepository = get<IAppRepository>(),
                logger = get<IAppLogger>(),
            )
        }
        factory { (serverId: Int) ->
            SpeedTestScreenViewModel(
                appRepository = get<IAppRepository>(),
                logger = get<IAppLogger>(),
                serverId = serverId,
            )
        }
        factory {
            HistoryScreenViewModel(
                appRepository = get<IAppRepository>(),
                logger = get<IAppLogger>(),
            )
        }
        factory {
            SettingsScreenViewModel(
                appRepository = get<IAppRepository>(),
                logger = get<IAppLogger>(),
            )
        }
    }
