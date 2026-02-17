package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.app_name
import netpulse_kmp.composeapp.generated.resources.history
import netpulse_kmp.composeapp.generated.resources.settings
import netpulse_kmp.composeapp.generated.resources.speed_test
import org.jetbrains.compose.resources.stringResource

data class TopBarState(
    val title: String,
    val showActions: Boolean,
    val showBackIcon: Boolean,
)

@Composable
fun currentTopBarState(backStack: NavBackStack<NavKey>): TopBarState {
    val current = backStack.lastOrNull()

    val isHome = current == Screen.Home

    val title =
        when (current) {
            Screen.Home -> stringResource(Res.string.app_name)
            is Screen.SpeedTest -> stringResource(Res.string.speed_test)
            Screen.History -> stringResource(Res.string.history)
            Screen.Settings -> stringResource(Res.string.settings)
            else -> ""
        }

    return TopBarState(
        title = title,
        showActions = isHome,
        showBackIcon = !isHome,
    )
}
