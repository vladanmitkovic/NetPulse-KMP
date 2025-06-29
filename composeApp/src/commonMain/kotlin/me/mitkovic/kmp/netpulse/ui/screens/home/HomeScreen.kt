package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onNavigateToSpeedTest: (Int) -> Unit,
) {
    val serverState = viewModel.serverFlow.collectAsStateWithLifecycle().value
    val nearestServerState = viewModel.nearestServerUiState.collectAsStateWithLifecycle().value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            Button(
                onClick = { viewModel.findNearestServer() },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
            ) {
                Text("Find Nearest Server")
            }

            when (nearestServerState) {
                is NearestServerUiState.Success -> {
                    if (nearestServerState.nearestServer != null) {
                        Text("Nearest Server: ${nearestServerState.nearestServer.name} (${nearestServerState.nearestServer.host})")
                        Text("") // Spacer
                        Button(
                            onClick = { onNavigateToSpeedTest(nearestServerState.nearestServer.id) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                        ) {
                            Text("Start Speed Test")
                        }
                    }
                }
                is NearestServerUiState.Error -> {
                    Text("Error: ${nearestServerState.error}")
                }
                is NearestServerUiState.Loading -> {}
            }

            when (serverState) {
                is SpeedTestServersUiState.Success -> {
                    Text("Success")
                }
                is SpeedTestServersUiState.Error -> {
                    Text("Error: ${serverState.error}")
                }
                is SpeedTestServersUiState.Loading -> {}
            }
        }

        if (serverState is SpeedTestServersUiState.Loading || nearestServerState is NearestServerUiState.Loading) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Text("Loading...", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
