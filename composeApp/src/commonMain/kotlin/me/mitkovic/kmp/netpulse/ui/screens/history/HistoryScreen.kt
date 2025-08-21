package me.mitkovic.kmp.netpulse.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.mitkovic.kmp.netpulse.domain.model.TestHistory
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.components.LinearChart
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun HistoryScreen(viewModel: HistoryScreenViewModel) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val logger: AppLogger = koinInject()

    if (history.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "No speed tests performed yet.")
        }
    } else {
        logger.logDebug("HistoryScreen", "Displaying ${history.size} test sessions")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(history) { item ->
                logger.logDebug(
                    "HistoryScreen",
                    "Rendering session ${item.sessionId}: downloadSpeed=${item.downloadSpeed}, uploadSpeed=${item.uploadSpeed}",
                )
                TestHistoryItem(item, onDelete = { viewModel.deleteSession(item.sessionId) })
            }
        }
    }
}

@Composable
fun TestHistoryItem(
    item: TestHistory,
    onDelete: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Header Section with Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = formatTimestamp(item.timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${item.serverName}, ${item.serverCountry}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = "${item.serverSponsor}, ${item.serverDistance.let { (it / 1000).roundToInt() }} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .padding(start = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Metrics Section
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Column 1: Ping, Jitter, Packet Loss (33% width)
                Column(
                    modifier = Modifier.weight(0.33f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MetricItem(label = "Ping", value = item.ping?.roundToInt() ?: "N/A", unit = "ms")
                    MetricItem(label = "Jitter", value = item.jitter?.roundToInt() ?: "N/A", unit = "ms")
                    MetricItem(label = "Packet Loss", value = item.packetLoss?.roundToInt() ?: "N/A", unit = "%")
                }

                // Column 2: Download and Upload text (43% width)
                Column(
                    modifier = Modifier.weight(0.43f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Download Text
                    Column {
                        Text(
                            text = "DOWNLOAD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${item.downloadSpeed?.roundToInt() ?: "N/A"} Mbps",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    // Upload Text
                    Column {
                        Text(
                            text = "UPLOAD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${item.uploadSpeed?.roundToInt() ?: "N/A"} Mbps",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Column 3: Line Charts (23% width)
                Column(
                    modifier = Modifier.weight(0.23f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Download Chart
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                    ) {
                        LinearChart(
                            speeds = item.downloadSpeeds,
                            lineColor = Color.Blue,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    // Upload Chart
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                    ) {
                        LinearChart(
                            speeds = item.uploadSpeeds,
                            lineColor = Color.Red,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: Any,
    unit: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.time.hour}:${localDateTime.time.minute.toString().padStart(2, '0')}"
}
