package me.mitkovic.kmp.netpulse

import androidx.compose.ui.window.ComposeUIViewController
import me.mitkovic.kmp.netpulse.di.initKoin
import me.mitkovic.kmp.netpulse.ui.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController {
        App()
    }
}
