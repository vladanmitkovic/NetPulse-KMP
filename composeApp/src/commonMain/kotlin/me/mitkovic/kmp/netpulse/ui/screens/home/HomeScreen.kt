package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onNavigateToSpeedTest: (Int) -> Unit,
) {
    val serverState = viewModel.serverFlow.collectAsStateWithLifecycle().value
    val nearestServerByLocationState = viewModel.nearestServerByLocationUiState.collectAsStateWithLifecycle().value
    val sortedServers by viewModel.sortedServersUiState.collectAsStateWithLifecycle()
    // val nearestServerState = viewModel.nearestServerUiState.collectAsStateWithLifecycle().value

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
    ) {
        when (nearestServerByLocationState) {
            is NearestServerByLocationUiState.Success -> {
                if (nearestServerByLocationState.nearestServer != null) {
                    Column(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = "Nearest server by location:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Country: ${nearestServerByLocationState.nearestServer.country}",
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Name: ${nearestServerByLocationState.nearestServer.name}",
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Sponsor: ${nearestServerByLocationState.nearestServer.sponsor}",
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Distance: ${nearestServerByLocationState.nearestServer.distance?.let {
                                (it / 1000).roundToInt()
                            } ?: "N/A"} km",
                            fontSize = 12.sp,
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = { onNavigateToSpeedTest(nearestServerByLocationState.nearestServer.id) },
                            shape = CircleShape,
                            modifier = Modifier.size(200.dp),
                        ) {
                            Text("Start Speed Test")
                        }
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            TextField(
                                readOnly = true,
                                value = nearestServerByLocationState.nearestServer.name,
                                onValueChange = { },
                                label = { Text("Select Server") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors =
                                    ExposedDropdownMenuDefaults.textFieldColors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                sortedServers.forEach { server ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${server.name} - ${server.sponsor} - ${server.country} - ${
                                                    server.distance?.let { (it / 1000).roundToInt() } ?: "N/A"
                                                } km",
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectServer(server)
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is NearestServerByLocationUiState.Error -> {
                Text(
                    text = "Error (Location): ${nearestServerByLocationState.error}",
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(vertical = 8.dp),
                )
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
                        .padding(16.dp)
                        .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Text("Finding nearest server...", modifier = Modifier.padding(top = 8.dp))
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
