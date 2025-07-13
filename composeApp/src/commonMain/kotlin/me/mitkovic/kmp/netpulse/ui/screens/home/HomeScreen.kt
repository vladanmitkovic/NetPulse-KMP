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
    val nearestServerByLocationState = viewModel.nearestServerByLocationUiState.collectAsStateWithLifecycle().value
    // val nearestServerState = viewModel.nearestServerUiState.collectAsStateWithLifecycle().value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (nearestServerByLocationState) {
                is NearestServerByLocationUiState.Success -> {
                    if (nearestServerByLocationState.nearestServer != null) {
                        Text(
                            "Nearest Server by Location: ${nearestServerByLocationState.nearestServer.name} (${nearestServerByLocationState.nearestServer.host})",
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                        Button(
                            onClick = { onNavigateToSpeedTest(nearestServerByLocationState.nearestServer.id) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                        ) {
                            Text("Start Speed Test (Location)")
                        }
                    }
                }
                is NearestServerByLocationUiState.Error -> {
                    Text("Error (Location): ${nearestServerByLocationState.error}")
                }
                is NearestServerByLocationUiState.Loading -> {}
            }

            if (serverState is ServersUiState.Loading ||
                nearestServerByLocationState is NearestServerByLocationUiState.Loading
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Text("Finding nearest server...", modifier = Modifier.padding(top = 8.dp))
                }
            }

            /*
            when (nearestServerState) {
                is NearestServerUiState.Success -> {
                    if (nearestServerState.nearestServer != null) {
                        Text(
                            "Nearest Server: ${nearestServerState.nearestServer.name} (${nearestServerState.nearestServer.host})",
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
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
             */
        }
    }
}
