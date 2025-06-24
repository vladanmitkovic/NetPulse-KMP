package me.mitkovic.kmp.netpulse.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger
import org.koin.compose.koinInject

@Composable
fun App() {
    val appLogger: AppLogger = koinInject<AppLogger>()

    appLogger.logDebug("GILE", "App Start from: " + Greeting().greet())

    val appViewModel: AppViewModel = koinInject<AppViewModel>()

    val uiState = appViewModel.conversionRatesUiState.collectAsStateWithLifecycle()
    val state = uiState.value

    val servers: List<Server> =
        when (state) {
            is SpeedTestServersUiState.Success -> state.servers
            else -> emptyList()
        }

    // appLogger.logDebug("GILE", "state: " + state)
    appLogger.logDebug("GILE", "servers: " + servers)

    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .safeContentPadding()
                    .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val greeting = remember { Greeting().greet() }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Compose: $greeting")
            }
        }
    }
}
