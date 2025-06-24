package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.ui.Greeting

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel) {
    viewModel.logDebug(Greeting().greet())

    val uiState = viewModel.conversionRatesUiState.collectAsStateWithLifecycle()
    val state = uiState.value

    val servers: List<Server> =
        when (state) {
            is SpeedTestServersUiState.Success -> state.servers
            else -> emptyList()
        }

    // appLogger.logDebug("GILE", "state: " + state)
    viewModel.logDebug("servers: " + servers)
}
