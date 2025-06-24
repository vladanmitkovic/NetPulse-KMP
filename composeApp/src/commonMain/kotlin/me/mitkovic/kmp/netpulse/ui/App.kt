package me.mitkovic.kmp.netpulse.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavHost
import org.koin.compose.koinInject

@Composable
fun App() {
    val appLogger: AppLogger = koinInject<AppLogger>()

    appLogger.logDebug("App", "App Start from: " + Greeting().greet())

    val appViewModel: AppViewModel = koinInject()

    val navController = rememberNavController()

    MaterialTheme {
        AppNavHost(
            navHostController = navController,
        )
    }
}
