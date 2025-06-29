package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import me.mitkovic.kmp.netpulse.data.local.database.SpeedTestResultEntity
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedTestScreen(viewModel: SpeedTestScreenViewModel) {
    val serverState by viewModel.serverUiState.collectAsStateWithLifecycle()
    val serverStateValue = serverState // Enable smart cast
    val databaseState by viewModel.databaseUiState.collectAsStateWithLifecycle()
    val databaseStateValue = databaseState // Capture value to enable smart cast

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        when (serverStateValue) {
            is ServerUiState.Loading -> {
                Text("Loading server...", modifier = Modifier.padding(16.dp))
            }
            is ServerUiState.Success -> {
                if (serverStateValue.server != null) {
                    Text(text = serverStateValue.server.toString())

                    viewModel.startSpeedTest(serverStateValue.server)
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
                Speedometer(result = databaseStateValue.result)
            }
            is DatabaseUiState.Error -> {
                Text("Result Error: ${databaseStateValue.error}", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

private fun toRadians(degrees: Double): Double = degrees * (kotlin.math.PI / 180.0)

@Composable
fun Speedometer(result: SpeedTestResultEntity?) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (result == null) {
            Text("No result available")
        } else {
            val title = if (result.testType == 1L) "Download" else "Upload"
            val speed = result.speed?.toFloat() ?: 0f
            val maxSpeed = 200f // Max speed for the speedometer (Mbps)
            val angle = (speed / maxSpeed) * 180f // Map speed to 0-180 degrees

            Text(
                text = "$title Speed",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Box(
                modifier = Modifier.size(220.dp), // Increased size to accommodate labels
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier =
                        Modifier
                            .size(200.dp)
                            .padding(16.dp),
                ) {
                    // Draw background arc (full gauge)
                    drawArc(
                        color = Color.Gray,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        style = Stroke(width = 10f),
                    )

                    // Draw speed arc (filled portion)
                    drawArc(
                        color = if (result.testType == 1L) Color.Blue else Color.Green,
                        startAngle = 180f,
                        sweepAngle = angle,
                        useCenter = false,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        style = Stroke(width = 10f),
                    )

                    // Draw needle
                    val needleLength = size.width / 2.5f
                    val needleAngle = toRadians((180 + angle).toDouble()).toFloat()
                    val needleEndX = (size.width / 2 + needleLength * cos(needleAngle)).toFloat()
                    val needleEndY = (size.height / 2 + needleLength * sin(needleAngle)).toFloat()
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width / 2, size.height / 2),
                        end = Offset(needleEndX, needleEndY),
                        strokeWidth = 5f,
                    )
                }

                // Minimum label (0 Mbps) at left (0°)
                Text(
                    text = "0 Mbps",
                    fontSize = 12.sp,
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 10.dp),
                )

                // Maximum label (200 Mbps) at right (180°)
                Text(
                    text = "200 Mbps",
                    fontSize = 12.sp,
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 10.dp),
                )
            }

            Text(
                text = "$speed Mbps",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
