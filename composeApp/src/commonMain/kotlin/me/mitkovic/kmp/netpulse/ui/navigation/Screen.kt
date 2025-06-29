package me.mitkovic.kmp.netpulse.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    object Home : Screen()

    @Serializable
    data class SpeedTest(
        val serverId: Int,
    ) : Screen()
}
