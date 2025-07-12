package me.mitkovic.kmp.netpulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.navigation.AppNavHost
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    koinInject<AppViewModel>()
    val appLogger: AppLogger = koinInject()
    appLogger.logDebug("App", "App Start from: ${Greeting().greet()}")

    val navController = rememberNavController()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("NetPulse") })
            },
        ) { innerPadding ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppNavHost(navHostController = navController)
            }
        }
    }
}
