package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.mitkovic.kmp.netpulse.ui.MainAction
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreen
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreen
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreen
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreen
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import netpulse_kmp.composeapp.generated.resources.Res
import netpulse_kmp.composeapp.generated.resources.app_name
import netpulse_kmp.composeapp.generated.resources.history
import netpulse_kmp.composeapp.generated.resources.settings
import netpulse_kmp.composeapp.generated.resources.speed_test
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(
    navHostController: NavController,
    onAction: (MainAction) -> Unit,
) {
    NavHost(
        navController = navHostController as NavHostController,
        startDestination = Screen.Home,
    ) {
        composable<Screen.Home> {
            onAction(MainAction.TitleTextChanged(stringResource(Res.string.app_name)))
            onAction(MainAction.ShowActionsChanged(true))
            onAction(MainAction.ShowBackIconChanged(false))

            val homeScreenViewModel: HomeScreenViewModel = koinInject<HomeScreenViewModel>()
            HomeScreen(
                viewModel = homeScreenViewModel,
                onNavigateToSpeedTest = { serverId ->
                    navHostController.navigate(Screen.SpeedTest(serverId))
                },
            )
        }

        composable<Screen.SpeedTest> { backStackEntry ->
            onAction(MainAction.TitleTextChanged(stringResource(Res.string.speed_test)))
            onAction(MainAction.ShowActionsChanged(false))
            onAction(MainAction.ShowBackIconChanged(true))

            val serverId = backStackEntry.arguments?.getInt("serverId") ?: 0
            val speedTestViewModel: SpeedTestScreenViewModel = koinInject { parametersOf(serverId) }
            SpeedTestScreen(
                viewModel = speedTestViewModel,
                onBackClick = {
                    navHostController.navigate(
                        Screen.Home,
                    )
                },
            )
        }

        composable<Screen.History> {
            onAction(MainAction.TitleTextChanged(stringResource(Res.string.history)))
            onAction(MainAction.ShowActionsChanged(false))
            onAction(MainAction.ShowBackIconChanged(true))

            val historyScreenViewModel: HistoryScreenViewModel = koinInject<HistoryScreenViewModel>()
            HistoryScreen(
                viewModel = historyScreenViewModel,
                onBackClick = {
                    navHostController.navigate(
                        Screen.Home,
                    )
                },
            )
        }

        composable<Screen.Settings> {
            onAction(MainAction.TitleTextChanged(stringResource(Res.string.settings)))
            onAction(MainAction.ShowActionsChanged(false))
            onAction(MainAction.ShowBackIconChanged(true))

            val settingsScreenViewModel: SettingsScreenViewModel = koinInject<SettingsScreenViewModel>()
            SettingsScreen(
                viewModel = settingsScreenViewModel,
                onBackClick = {
                    navHostController.navigate(
                        Screen.Home,
                    )
                },
            )
        }
    }
}
