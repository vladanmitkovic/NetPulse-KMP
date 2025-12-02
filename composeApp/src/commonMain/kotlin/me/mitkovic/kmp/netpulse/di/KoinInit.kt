package me.mitkovic.kmp.netpulse.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.ksp.generated.module

// Platform-specific DI must be provided per target.
expect fun platformModule(): Module

fun initKoin(koinContext: KoinApplication.() -> Unit = {}) {
    try {
        startKoin {
            allowOverride(true)
            koinContext()
            modules(
                AppModule().module,
                DatabaseModule().module, // provides NetPulseDatabase
                LocalStorageModule().module, // provides ILocalStorage
                RemoteModule().module, // provides IRemoteService
                ViewModelModule().module,
                platformModule(), // Android/iOS/Desktop specific bindings
            )
        }
    } catch (e: Exception) {
        println("Koin init failed: ${e.message}")
        throw RuntimeException("Koin init failed", e)
    }
}
