package me.mitkovic.kmp.netpulse

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "NetPulse-KMP",
    ) {
        App()
    }
}