package me.mitkovic.kmp.netpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreen
import me.mitkovic.kmp.netpulse.ui.screens.home.HomeScreenViewModel
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
    }
}
