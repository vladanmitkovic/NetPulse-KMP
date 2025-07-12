package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.AppViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        factory {
            AppViewModel(
                appRepository = get<AppRepository>(),
                logger = get<AppLogger>(),
            )
        }
        factory {
            HomeScreenViewModel(
                appRepository = get<AppRepository>(),
                logger = get<AppLogger>(),
            )
        }
        factory { (serverId: Int) ->
            SpeedTestScreenViewModel(
                appRepository = get<AppRepository>(),
                logger = get<AppLogger>(),
                serverId = serverId,
            )
        }
    }
