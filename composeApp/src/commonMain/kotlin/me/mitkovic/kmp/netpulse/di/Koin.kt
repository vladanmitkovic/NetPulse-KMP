package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.remote.RemoteService
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.data.repository.AppRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.speedtest.SpeedTestRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.theme.ThemeRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.ThemeRepositoryImpl
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.platform.Platform
import me.mitkovic.kmp.netpulse.platform.getPlatform
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule =
    module {
        single<Platform> {
            getPlatform()
        }

        single<ThemeRepository> {
            ThemeRepositoryImpl(
                localStorage = get<LocalStorage>(),
            )
        }

        single<SettingsRepository> {
            SettingsRepositoryImpl(
                localStorage = get<LocalStorage>(),
            )
        }

        single<SpeedTestRepository> {
            SpeedTestRepositoryImpl(
                localStorage = get<LocalStorage>(),
                remoteService = get<RemoteService>(),
                settingsRepository = get<SettingsRepository>(),
                logger = get<AppLogger>(),
            )
        }

        single<AppRepository> {
            AppRepositoryImpl(
                speedTestRepository = get<SpeedTestRepository>(),
                themeRepository = get<ThemeRepository>(),
                settingsRepository = get<SettingsRepository>(),
            )
        }
    }

expect fun platformModule(): Module

fun initKoin(koinContext: KoinApplication.() -> Unit = {}) {
    try {
        startKoin {
            koinContext()
            modules(commonModule, platformModule(), viewModelModule)
        }
    } catch (e: Exception) {
        println("Koin init failed: ${e.message}") // Cross-platform fallback
        throw RuntimeException("Koin init failed", e)
    }
}
