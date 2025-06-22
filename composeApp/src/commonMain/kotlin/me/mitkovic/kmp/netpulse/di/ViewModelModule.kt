package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.AppViewModel
import org.koin.dsl.module

val viewModelModule =
    module {
        factory {
            AppViewModel(
                netPulseRepository = get<NetPulseRepository>(),
                logger = get<AppLogger>(),
            )
        }
    }
