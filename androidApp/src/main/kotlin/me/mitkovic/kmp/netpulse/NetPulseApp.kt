package me.mitkovic.kmp.netpulse

import android.app.Application
import me.mitkovic.kmp.netpulse.di.initKoin
import org.koin.android.ext.koin.androidContext
import timber.log.Timber

class NetPulseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initKoin {
            androidContext(this@NetPulseApp)
        }
    }
}
