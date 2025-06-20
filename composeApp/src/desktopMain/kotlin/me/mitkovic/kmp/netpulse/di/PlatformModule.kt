package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.logging.AppLoggerImpl
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<AppLogger> {
            AppLoggerImpl()
        }
    }
