package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestScreenViewModel,
    onBackClick: () -> Unit,
) {
    val serverState by viewModel.serverUiState.collectAsStateWithLifecycle()
    val serverStateValue = serverState
    val databaseState by viewModel.databaseFlow.collectAsStateWithLifecycle() // Changed to databaseFlow
    val databaseStateValue = databaseState
    val isTestCompleted by viewModel.databaseUiState.collectAsStateWithLifecycle() // Added for completion check

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        IconButton(
            onClick = { onBackClick() },
            modifier =
                Modifier
                    .padding(top = 16.dp, start = 16.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(16.dp),
            )
        }

        when (serverStateValue) {
            is ServerUiState.Loading -> {
                Text("Loading server...", modifier = Modifier.padding(16.dp))
            }
            is ServerUiState.Success -> {
                if (serverStateValue.server != null) {
                    Text(text = serverStateValue.server.toString())
                } else {
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

        when (databaseStateValue) {
            is DatabaseUiState.Loading -> {
                Text("Loading result...", modifier = Modifier.padding(top = 8.dp))
            }
            is DatabaseUiState.Success -> {
                Speedometer(result = databaseStateValue.result, isTestCompleted = isTestCompleted is DatabaseUiState.Completed)
            }
            is DatabaseUiState.Error -> {
                Text("Result Error: ${databaseStateValue.error}", modifier = Modifier.padding(top = 8.dp))
            }
            is DatabaseUiState.Completed -> {
                Speedometer(result = null, isTestCompleted = true)
            }
        }
    }
}

private fun toRadians(degrees: Double): Double = degrees * (kotlin.math.PI / 180.0)

@Composable
fun Speedometer(
    result: TestResult?,
    isTestCompleted: Boolean,
) {
    var lastDownloadSpeed by remember { mutableStateOf<Float?>(null) }
    var lastUploadSpeed by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isTestCompleted && lastDownloadSpeed != null && lastUploadSpeed != null) {
            // Show last download and upload speeds when tests are complete
            Text(
                text = "Download Speed: ${lastDownloadSpeed!!.roundToInt()} Mbps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = "Upload Speed: ${lastUploadSpeed!!.roundToInt()} Mbps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        } else if (result == null) {
            Text("No result available")
        } else {
            // Update last speeds and show speedometer
            val speed = result.speed?.toFloat() ?: 0f
            if (result.testType == 1L) {
                lastDownloadSpeed = speed
            } else if (result.testType == 2L) {
                lastUploadSpeed = speed
            }

            val title = if (result.testType == 1L) "Download" else "Upload"
            val maxSpeed = 1000f // Fixed maximum of 1000 Mbps
            val angle = (speed / maxSpeed) * 180f // Map speed to 0-180 degrees

            Text(
                text = "$title Speed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "0 Mbps",
                    fontSize = 12.sp,
                )
                Text(
                    text = "1000 Mbps",
                    fontSize = 12.sp,
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    // Square proportional to width
                ) {
                    // Draw background arc (full gauge)
                    drawArc(
                        color = Color.Gray,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(10f, 10f), // Offset for stroke width
                        size = Size(size.width - 20f, size.height - 20f), // Adjusted for stroke
                        style = Stroke(width = 20f),
                    )

                    // Draw speed arc (filled portion)
                    drawArc(
                        color = if (result.testType == 1L) Color.Blue else Color.Green,
                        startAngle = 180f,
                        sweepAngle = angle,
                        useCenter = false,
                        topLeft = Offset(10f, 10f), // Offset for stroke width
                        size = Size(size.width - 20f, size.height - 20f), // Adjusted for stroke
                        style = Stroke(width = 20f),
                    )

                    // Draw needle
                    val needleLength = size.width / 2f // Reaches arc edge
                    val needleAngle = toRadians((180 + angle).toDouble()).toFloat()
                    val needleEndX = (size.width / 2 + needleLength * cos(needleAngle))
                    val needleEndY = (size.height / 2 + needleLength * sin(needleAngle))
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width / 2, size.height / 2),
                        end = Offset(needleEndX, needleEndY),
                        strokeWidth = 10f,
                    )
                }
            }

            Text(
                text = "${speed.roundToInt()} Mbps",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
