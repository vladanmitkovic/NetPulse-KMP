package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
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
fun NavController.currentTopBarState(): TopBarState {
    val currentBackStackEntry by currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val isHome = currentDestination?.hasRoute(Screen.Home::class) ?: true

    return TopBarState(
        title =
            when {
                isHome -> stringResource(Res.string.app_name)
                currentDestination.hasRoute(Screen.SpeedTest::class) -> stringResource(Res.string.speed_test)
                currentDestination.hasRoute(Screen.History::class) -> stringResource(Res.string.history)
                currentDestination.hasRoute(Screen.Settings::class) -> stringResource(Res.string.settings)
                else -> ""
            },
        showActions = isHome,
        showBackIcon = !isHome,
    )
}
