package me.mitkovic.kmp.netpulse.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    object Home : Screen

    @Serializable
    data class SpeedTest(
        val serverId: Int,
    ) : Screen

    @Serializable
    object History : Screen

    @Serializable
    object Settings : Screen
}
