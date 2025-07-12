package me.mitkovic.kmp.netpulse

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.mitkovic.kmp.netpulse.di.initKoin
import me.mitkovic.kmp.netpulse.ui.App

fun main() {
    initKoin()
    application {
        Window(onCloseRequest = ::exitApplication, title = "NetPulse-KMP") {
            App()
        }
    }
}
