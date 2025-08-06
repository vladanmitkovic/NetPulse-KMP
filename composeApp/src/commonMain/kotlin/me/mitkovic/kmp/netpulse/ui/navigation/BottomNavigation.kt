package me.mitkovic.kmp.netpulse.ui.navigation

import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.history
import netpulse_kmp.composeapp.generated.resources.home
import netpulse_kmp.composeapp.generated.resources.ic_history
import netpulse_kmp.composeapp.generated.resources.ic_home
import netpulse_kmp.composeapp.generated.resources.ic_settings
import netpulse_kmp.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class BottomNavigation(
    val label: StringResource,
    val icon: DrawableResource,
    val route: Screen,
) {
    HOME(Res.string.home, Res.drawable.ic_home, Screen.Home),
    HISTORY(Res.string.history, Res.drawable.ic_history, Screen.History),
    SETTINGS(Res.string.settings, Res.drawable.ic_settings, Screen.Settings),
}
