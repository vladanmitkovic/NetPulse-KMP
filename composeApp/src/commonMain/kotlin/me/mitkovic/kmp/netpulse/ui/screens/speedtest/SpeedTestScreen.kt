package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import me.mitkovic.kmp.netpulse.common.Constants.DOWNLOAD_TIMEOUT
import me.mitkovic.kmp.netpulse.common.Constants.UPLOAD_TIMEOUT
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.ui.components.LinearChart
import me.mitkovic.kmp.netpulse.ui.components.SpeedGauge
import me.mitkovic.kmp.netpulse.ui.components.VerticalProgressIndicator
import kotlin.math.roundToInt

@Composable
fun SpeedTestScreen(viewModel: SpeedTestScreenViewModel) {
    val serverState by viewModel.serverUiState.collectAsStateWithLifecycle()
    val serverStateValue = serverState
    val databaseUiStateValue by viewModel.databaseUiState.collectAsStateWithLifecycle()
    val state = databaseUiStateValue
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    Column(
        modifier =
            Modifier
                .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        when (serverStateValue) {
            is ServerUiState.Loading -> {
                Text("Loading server...", modifier = Modifier.padding(16.dp))
            }
            is ServerUiState.Success -> {
                if (serverStateValue.server == null) {
                    Text("Server not found")
                }
            }
            is ServerUiState.Error -> {
                Text(
                    text = "Error: ${serverStateValue.error}",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        if (state is DatabaseUiState.Error) {
            Text("Result Error: ${state.error}", modifier = Modifier.padding(top = 8.dp))
        } else {
            Speedometer(
                progress = progress,
                isTestCompleted = state is DatabaseUiState.Completed,
                server = if (serverStateValue is ServerUiState.Success) serverStateValue.server else null,
                onRetest = {
                    viewModel.reset()
                    if (state is DatabaseUiState.Completed &&
                        serverStateValue is ServerUiState.Success &&
                        serverStateValue.server != null
                    ) {
                        viewModel.startSpeedTest(serverStateValue.server)
                    }
                },
            )
        }
    }
}

@Composable
fun Speedometer(
    progress: SpeedTestProgress,
    isTestCompleted: Boolean,
    server: Server? = null,
    onRetest: () -> Unit = {},
) {
    var lastDownloadSpeed by remember { mutableStateOf<Float?>(null) }
    var lastUploadSpeed by remember { mutableStateOf<Float?>(null) }
    var currentTestType by remember { mutableStateOf<Long?>(null) } // 1 for download, 2 for upload
    var previousTestType by remember { mutableStateOf<Long?>(null) }
    var downloadSpeeds by remember { mutableStateOf(listOf<Float>()) }
    var uploadSpeeds by remember { mutableStateOf(listOf<Float>()) }

    LaunchedEffect(progress.downloadSpeed) {
        if (progress.downloadSpeed != null && progress.downloadSpeed.toFloat() != lastDownloadSpeed) {
            val speed = progress.downloadSpeed.toFloat()
            if (1L != previousTestType) {
                downloadSpeeds = emptyList()
                previousTestType = 1L
            }
            downloadSpeeds += speed
            lastDownloadSpeed = speed
            currentTestType = 1L
        }
    }

    LaunchedEffect(progress.uploadSpeed) {
        if (progress.uploadSpeed != null && progress.uploadSpeed.toFloat() != lastUploadSpeed) {
            val speed = progress.uploadSpeed.toFloat()
            if (2L != previousTestType) {
                uploadSpeeds = emptyList()
                previousTestType = 2L
            }
            uploadSpeeds += speed
            lastUploadSpeed = speed
            currentTestType = 2L
        }
    }

    val downloadSpeed = lastDownloadSpeed ?: 0f
    val uploadSpeed = lastUploadSpeed ?: 0f
    val maxSpeed = 1000f
    val speed = if (currentTestType == 1L) downloadSpeed else uploadSpeed
    val angle = (speed / maxSpeed) * 180f
    val color = if (currentTestType == 1L) Color.Blue else Color.Red

    LaunchedEffect(isTestCompleted) {
        if (!isTestCompleted) {
            lastDownloadSpeed = null
            lastUploadSpeed = null
            currentTestType = null
            previousTestType = null
            downloadSpeeds = emptyList()
            uploadSpeeds = emptyList()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Show gauge or button at the top
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (!isTestCompleted) {
                SpeedGauge(
                    angle = angle,
                    width = 300,
                    height = 200,
                    arcColor = color,
                    modifier = Modifier,
                )
            } else {
                Button(
                    onClick = onRetest,
                    shape = CircleShape,
                    modifier = Modifier.size(200.dp),
                ) {
                    Text("Again")
                }
            }
        }

        // Two columns: left for Ping/Jitter/PacketLoss, right for Download/Upload
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Left column: Ping, Jitter, Packet Loss
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "PING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${(progress.ping ?: 0.0).roundToInt()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "ms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = "JITTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "${(progress.jitter ?: 0.0).roundToInt()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "ms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = "PACKET LOSS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "${(progress.packetLoss ?: 0.0).roundToInt()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            // Right column: Download and Upload
            Column(
                modifier = Modifier.weight(2f),
            ) {
                // Download section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        val isDownloading = currentTestType == 1L && !isTestCompleted
                        Text(
                            text = "DOWNLOAD",
                            fontSize = if (isDownloading) 17.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${downloadSpeed.roundToInt()}",
                            fontSize = if (isDownloading) 30.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Mbps",
                            fontSize = if (isDownloading) 17.sp else 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                    Row {
                        Box(modifier = Modifier.width(20.dp)) {
                            if (currentTestType == 1L || lastDownloadSpeed != null) {
                                VerticalProgressIndicator(
                                    progress = if (currentTestType != 1L || isTestCompleted) 1f else 0f,
                                    animate = currentTestType == 1L && !isTestCompleted,
                                    durationMillis = (DOWNLOAD_TIMEOUT * 1000).toInt(),
                                    reset = !isTestCompleted && currentTestType == null,
                                    complete = isTestCompleted || currentTestType == 2L,
                                    color = Color.Blue,
                                    fromBottom = false,
                                    modifier =
                                        Modifier
                                            .width(4.dp)
                                            .height(60.dp)
                                            .align(Alignment.Center),
                                )
                            }
                        }
                        Box(modifier = Modifier.width(100.dp).height(60.dp)) {
                            if (currentTestType == 1L || lastDownloadSpeed != null) {
                                LinearChart(
                                    speeds = downloadSpeeds,
                                    lineColor = Color.Blue,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }

                // Upload section
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        val isUploading = currentTestType == 2L && !isTestCompleted
                        Text(
                            text = "UPLOAD",
                            fontSize = if (isUploading) 17.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${uploadSpeed.roundToInt()}",
                            fontSize = if (isUploading) 30.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Mbps",
                            fontSize = if (isUploading) 17.sp else 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                    Row {
                        Box(modifier = Modifier.width(20.dp)) {
                            if (currentTestType == 2L || lastUploadSpeed != null) {
                                VerticalProgressIndicator(
                                    progress = if (isTestCompleted) 1f else 0f,
                                    animate = currentTestType == 2L && !isTestCompleted,
                                    durationMillis = (UPLOAD_TIMEOUT * 1000).toInt(),
                                    reset = !isTestCompleted && currentTestType == null,
                                    complete = isTestCompleted,
                                    color = Color.Red,
                                    fromBottom = true,
                                    modifier =
                                        Modifier
                                            .width(4.dp)
                                            .height(60.dp)
                                            .align(Alignment.Center),
                                )
                            }
                        }
                        Box(modifier = Modifier.width(100.dp).height(60.dp)) {
                            if (currentTestType == 2L || lastUploadSpeed != null) {
                                LinearChart(
                                    speeds = uploadSpeeds,
                                    lineColor = Color.Red,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server details
        if (server != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Country: ${server.country}",
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "Name: ${server.name}",
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "Sponsor: ${server.sponsor}",
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "Distance: ${server.distance?.let { (it / 1000).roundToInt() } ?: "N/A"} km",
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}
