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
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule =
    module {
        single<Platform> {
            getPlatform()
        }

        single<IAppLogger> {
            AppLoggerImpl()
        }

        // Default XML config (can be overridden in platform modules)
        single<XML> {
            XML {
                indentString = "  "
                repairNamespaces = true
            }
        }

        single<IThemeRepository> {
            ThemeRepositoryImpl(
                localStorage = get<ILocalStorage>(),
            )
        }

        single<ISettingsRepository> {
            SettingsRepositoryImpl(
                localStorage = get<ILocalStorage>(),
            )
        }

        single<ISpeedTestRepository> {
            SpeedTestRepositoryImpl(
                localStorage = get<ILocalStorage>(),
                remoteService = get<IRemoteService>(),
                settingsRepository = get<ISettingsRepository>(),
                logger = get<IAppLogger>(),
            )
        }

        single<IAppRepository> {
            AppRepositoryImpl(
                speedTestRepository = get<ISpeedTestRepository>(),
                themeRepository = get<IThemeRepository>(),
                settingsRepository = get<ISettingsRepository>(),
            )
        }
    }

// Platform-specific DI must be provided per target.
expect fun platformModule(): Module

fun initKoin(koinContext: KoinApplication.() -> Unit = {}) {
    try {
        startKoin {
            // allow platform bindings to replace common ones
            allowOverride(true)
            koinContext()
            modules(commonModule, platformModule(), viewModelModule)
        }
    } catch (e: Exception) {
        println("Koin init failed: ${e.message}") // Cross-platform fallback
        throw RuntimeException("Koin init failed", e)
    }
}
