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

@Composable
fun AppNavHost(navHostController: NavController) {
    NavHost(
        navController = navHostController as NavHostController,
        startDestination = Screen.Home,
    ) {
        // CurrencyConverter screen
        composable<Screen.Home> {
            val homeScreenVeiwModel: HomeScreenViewModel = koinInject<HomeScreenViewModel>()
            HomeScreen(
                viewModel = homeScreenVeiwModel,
            )
        }

        // Favorites screen
        composable<Screen.SpeedTest> {
            val favoritesViewModel: SpeedTestScreenViewModel = koinInject<SpeedTestScreenViewModel>()
            SpeedTestScreen(
                viewModel = favoritesViewModel,
            )
        }
    }
}
