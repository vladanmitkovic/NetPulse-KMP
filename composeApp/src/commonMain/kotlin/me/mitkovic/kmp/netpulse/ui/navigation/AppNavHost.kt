package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreen
import me.mitkovic.kmp.netpulse.ui.screens.history.HistoryScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreen
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreen
import me.mitkovic.kmp.netpulse.ui.screens.settings.SettingsScreenViewModel
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreen
import me.mitkovic.kmp.netpulse.ui.screens.speedtest.SpeedTestScreenViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(navHostController: NavController) {
    NavHost(
        navController = navHostController as NavHostController,
        startDestination = Screen.Home,
    ) {
        composable<Screen.Home> {
            val homeScreenViewModel: HomeScreenViewModel = koinInject<HomeScreenViewModel>()
            HomeScreen(
                viewModel = homeScreenViewModel,
                onNavigateToSpeedTest = { serverId ->
                    navHostController.navigate(Screen.SpeedTest(serverId))
                },
            )
        }

        composable<Screen.SpeedTest> { backStackEntry ->
            val args: Screen.SpeedTest = backStackEntry.toRoute()
            val viewModel: SpeedTestScreenViewModel = koinInject { parametersOf(args.serverId) }

            SpeedTestScreen(viewModel = viewModel)
        }

        composable<Screen.History> {
            val historyScreenViewModel: HistoryScreenViewModel = koinInject<HistoryScreenViewModel>()
            HistoryScreen(
                viewModel = historyScreenViewModel,
            )
        }

        composable<Screen.Settings> {
            val settingsScreenViewModel: SettingsScreenViewModel = koinInject<SettingsScreenViewModel>()
            SettingsScreen(
                viewModel = settingsScreenViewModel,
            )
        }
    }
}
