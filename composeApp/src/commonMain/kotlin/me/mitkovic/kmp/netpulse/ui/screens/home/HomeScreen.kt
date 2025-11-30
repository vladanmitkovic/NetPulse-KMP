package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    onNavigateToSpeedTest: (Int) -> Unit,
) {
    val serverState = viewModel.serverFlow.collectAsStateWithLifecycle().value
    val nearestServerByLocationState = viewModel.nearestServerByLocationUiState.collectAsStateWithLifecycle().value
    val sortedServers by viewModel.sortedServersUiState.collectAsStateWithLifecycle()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.medium),
    ) {
        when (nearestServerByLocationState) {
            is NearestServerByLocationUiState.Success -> {
                if (nearestServerByLocationState.nearestServer != null) {
                    Button(
                        onClick = { onNavigateToSpeedTest(nearestServerByLocationState.nearestServer.id) },
                        shape = CircleShape,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(MaterialTheme.spacing.buttonSizeLarge),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                            ),
                    ) {
                        Text(
                            text = "GO",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = MaterialTheme.spacing.medium)
                                .clip(RoundedCornerShape(MaterialTheme.spacing.medium)),
                    ) {
                        TextField(
                            readOnly = true,
                            value = nearestServerByLocationState.nearestServer.sponsor,
                            onValueChange = { },
                            label = {
                                Text(
                                    text = "Select Server",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors =
                                ExposedDropdownMenuDefaults.textFieldColors(
                                    focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                                    disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                    errorContainerColor = MaterialTheme.colorScheme.secondary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            textStyle = MaterialTheme.typography.labelSmall,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .background(MaterialTheme.colorScheme.secondary),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary),
                        ) {
                            sortedServers.forEach { serverUi ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)) {
                                            Column(modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)) {
                                                Text(
                                                    text = serverUi.sponsor,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = Bold,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )
                                                Text(
                                                    text =
                                                        buildAnnotatedString {
                                                            append(serverUi.locationText)
                                                            withStyle(
                                                                style =
                                                                    SpanStyle(
                                                                        color = MaterialTheme.colorScheme.primary,
                                                                        fontWeight = Normal,
                                                                    ),
                                                            ) {
                                                                append(serverUi.formattedDistance)
                                                            }
                                                        },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectServer(serverUi.id)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
            is NearestServerByLocationUiState.Error -> {
                Text(
                    text = nearestServerByLocationState.errorText,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(vertical = MaterialTheme.spacing.small),
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
                        .padding(MaterialTheme.spacing.medium)
                        .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Text("Finding nearest server...", modifier = Modifier.padding(top = MaterialTheme.spacing.small))
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
