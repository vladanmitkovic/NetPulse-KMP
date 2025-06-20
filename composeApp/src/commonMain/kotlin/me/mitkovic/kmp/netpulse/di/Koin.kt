package me.mitkovic.kmp.netpulse.di

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
    }

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        modules(commonModule, platformModule())
        appDeclaration()
    }
}
