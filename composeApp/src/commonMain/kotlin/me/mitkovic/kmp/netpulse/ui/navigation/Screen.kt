package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {

    @Serializable
    data object Home : Screen

    @Serializable
    data class SpeedTest(
        val serverId: Int,
    ) : Screen

    @Serializable
    data object History : Screen

    @Serializable
    data object Settings : Screen
}
