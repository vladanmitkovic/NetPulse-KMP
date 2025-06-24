package me.mitkovic.kmp.netpulse.di

import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.remote.RemoteDataSource
import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepository
import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepositoryImpl
import me.mitkovic.kmp.netpulse.data.repository.speedtestservers.SpeedTestServersRepositoryImpl
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestServersRepository
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

        single<SpeedTestServersRepository> {
            SpeedTestServersRepositoryImpl(
                localDataSource = get<LocalDataSource>(),
                remoteDataSource = get<RemoteDataSource>(),
                logger = get<AppLogger>(),
            )
        }

        single<NetPulseRepository> {
            NetPulseRepositoryImpl(
                speedTestServersRepository = get<SpeedTestServersRepository>(),
            )
        }
    }

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        modules(commonModule, platformModule(), viewModelModule)
        appDeclaration()
    }
}
