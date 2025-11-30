package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.common.Constants.DOWNLOAD_TIMEOUT
import me.mitkovic.kmp.netpulse.common.Constants.UPLOAD_TIMEOUT
import me.mitkovic.kmp.netpulse.ui.components.LinearChart
import me.mitkovic.kmp.netpulse.ui.components.SpeedGauge
import me.mitkovic.kmp.netpulse.ui.components.VerticalProgressIndicator
import me.mitkovic.kmp.netpulse.ui.theme.spacing

@Composable
fun SpeedTestScreen(viewModel: SpeedTestScreenViewModel) {
    val serverState by viewModel.serverUiState.collectAsStateWithLifecycle()
    val databaseState by viewModel.databaseUiState.collectAsStateWithLifecycle()
    val progressUi by viewModel.progressUi.collectAsStateWithLifecycle()
    val serverInfoUi by viewModel.serverInfoUi.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeedTest()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        when (serverState) {
            is ServerUiState.Loading -> {
                Text(
                    text = "Loading server...",
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            is ServerUiState.Success -> {
                if ((serverState as ServerUiState.Success).server == null) {
                    Text(
                        text = "Server not found",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            is ServerUiState.Error -> {
                Text(
                    text = (serverState as ServerUiState.Error).errorText,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        when (databaseState) {
            is DatabaseUiState.Error -> {
                Text(
                    text = (databaseState as DatabaseUiState.Error).errorText,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            else -> {
                Speedometer(
                    progressUi = progressUi,
                    isTestCompleted = databaseState is DatabaseUiState.Completed,
                    onRetest = {
                        viewModel.reset()
                        val server = (serverState as? ServerUiState.Success)?.server
                        if (server != null) {
                            viewModel.startSpeedTest(server)
                        }
                    },
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = MaterialTheme.spacing.medium),
                ) {
                    if (serverInfoUi != null) {
                        ServerInfoCard(
                            serverInfoUi = serverInfoUi!!,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = MaterialTheme.spacing.medium),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerInfoCard(
    serverInfoUi: ServerInfoUi,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.small),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = serverInfoUi.sponsor,
                fontWeight = Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text =
                    buildAnnotatedString {
                        append(serverInfoUi.locationText)
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(serverInfoUi.formattedDistance)
                        }
                    },
                fontWeight = Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun Speedometer(
    progressUi: SpeedTestUi,
    isTestCompleted: Boolean,
    onRetest: () -> Unit = {},
) {
    var lastDownloadSpeed by remember { mutableStateOf<Float?>(null) }
    var lastUploadSpeed by remember { mutableStateOf<Float?>(null) }
    var currentTestType by remember { mutableStateOf<Long?>(null) }
    var previousTestType by remember { mutableStateOf<Long?>(null) }
    var downloadSpeeds by remember { mutableStateOf(listOf<Float>()) }
    var uploadSpeeds by remember { mutableStateOf(listOf<Float>()) }

    LaunchedEffect(progressUi.downloadSpeed) {
        if (progressUi.downloadSpeed != null && progressUi.downloadSpeed != lastDownloadSpeed) {
            val speed = progressUi.downloadSpeed
            if (1L != previousTestType) {
                downloadSpeeds = emptyList()
                previousTestType = 1L
            }
            downloadSpeeds += speed
            lastDownloadSpeed = speed
            currentTestType = 1L
        }
    }

    LaunchedEffect(progressUi.uploadSpeed) {
        if (progressUi.uploadSpeed != null && progressUi.uploadSpeed != lastUploadSpeed) {
            val speed = progressUi.uploadSpeed
            if (2L != previousTestType) {
                uploadSpeeds = emptyList()
                previousTestType = 2L
            }
            uploadSpeeds += speed
            lastUploadSpeed = speed
            currentTestType = 2L
        }
    }

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

    val downloadSpeed = lastDownloadSpeed ?: 0f
    val uploadSpeed = lastUploadSpeed ?: 0f
    val maxSpeed = 1000f
    val speed = if (currentTestType == 1L) downloadSpeed else uploadSpeed
    val angle = (speed / maxSpeed) * 180f
    val isDownloading = currentTestType == 1L && !isTestCompleted
    val isUploading = currentTestType == 2L && !isTestCompleted
    val color = MaterialTheme.colorScheme.tertiary
    val spacing = MaterialTheme.spacing

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = spacing.medium, end = spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                    modifier = Modifier.size(spacing.buttonSizeLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                ) {
                    Text(
                        text = "Again",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xLarge),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "PING",
                    fontSize = 12.sp,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = progressUi.pingText,
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "JITTER",
                    fontSize = 12.sp,
                    fontWeight = Bold,
                    modifier = Modifier.padding(top = spacing.small),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = progressUi.jitterText,
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "PCKT. LOSS",
                    fontSize = 12.sp,
                    fontWeight = Bold,
                    modifier = Modifier.padding(top = spacing.small),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = progressUi.packetLossText,
                    fontSize = 20.sp,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(
                modifier = Modifier.weight(2f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        val isDownloadingNow = isDownloading
                        Text(
                            text = "DOWNLOAD",
                            fontSize = if (isDownloadingNow) 17.sp else 12.sp,
                            fontWeight = Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = progressUi.downloadSpeedText,
                            fontSize = if (isDownloadingNow) 30.sp else 20.sp,
                            fontWeight = Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Mbps",
                            fontSize = if (isDownloadingNow) 20.sp else 17.sp,
                            fontWeight = Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Row {
                        Box(modifier = Modifier.width(spacing.small20)) {
                            if (currentTestType == 1L || lastDownloadSpeed != null) {
                                VerticalProgressIndicator(
                                    progress = if (currentTestType != 1L || isTestCompleted) 1f else 0f,
                                    animate = currentTestType == 1L && !isTestCompleted,
                                    durationMillis = (DOWNLOAD_TIMEOUT * 1000).toInt(),
                                    reset = !isTestCompleted && currentTestType == null,
                                    complete = isTestCompleted || currentTestType == 2L,
                                    color = color,
                                    fromBottom = false,
                                    modifier =
                                        Modifier
                                            .width(spacing.progressIndicatorWidth)
                                            .height(spacing.chartHeightLarge)
                                            .align(Alignment.Center),
                                )
                            }
                        }
                        Box(modifier = Modifier.width(spacing.chartWidth).height(spacing.chartHeightLarge)) {
                            if (currentTestType == 1L || lastDownloadSpeed != null) {
                                LinearChart(
                                    speeds = downloadSpeeds,
                                    lineColor = color,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        val isUploadingNow = isUploading
                        Text(
                            text = "UPLOAD",
                            fontSize = if (isUploadingNow) 17.sp else 12.sp,
                            fontWeight = Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = progressUi.uploadSpeedText,
                            fontSize = if (isUploadingNow) 30.sp else 20.sp,
                            fontWeight = Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Mbps",
                            fontSize = if (isUploadingNow) 20.sp else 17.sp,
                            fontWeight = Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Row {
                        Box(
                            modifier =
                                Modifier
                                    .width(spacing.small20)
                                    .height(spacing.chartHeightLarge),
                        ) {
                            if (currentTestType == 2L || lastUploadSpeed != null) {
                                VerticalProgressIndicator(
                                    progress = if (isTestCompleted) 1f else 0f,
                                    animate = currentTestType == 2L && !isTestCompleted,
                                    durationMillis = (UPLOAD_TIMEOUT * 1000).toInt(),
                                    reset = !isTestCompleted && currentTestType == null,
                                    complete = isTestCompleted,
                                    color = color,
                                    fromBottom = true,
                                    modifier =
                                        Modifier
                                            .width(spacing.progressIndicatorWidth)
                                            .height(spacing.chartHeightLarge)
                                            .align(Alignment.Center),
                                )
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .width(spacing.chartWidth)
                                    .height(spacing.chartHeightLarge),
                        ) {
                            if (currentTestType == 2L || lastUploadSpeed != null) {
                                LinearChart(
                                    speeds = uploadSpeeds,
                                    lineColor = color,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
