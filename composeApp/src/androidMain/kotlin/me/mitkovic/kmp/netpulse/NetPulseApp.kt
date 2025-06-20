package me.mitkovic.kmp.netpulse

import android.app.Application
import me.mitkovic.kmp.netpulse.di.initKoin
import org.koin.android.ext.koin.androidContext

class NetPulseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@NetPulseApp)
        }
    }
}
