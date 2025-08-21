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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.mitkovic.kmp.netpulse.domain.model.TestHistory
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.ui.components.LinearChart
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(viewModel: HistoryScreenViewModel) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val logger: AppLogger = koinInject()

    if (history.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
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
                TestHistoryItem(item)
            }
        }
    }
}

@Composable
fun TestHistoryItem(item: TestHistory) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatTimestamp(item.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${item.serverName}, ${item.serverCountry}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ping: ${item.ping?.roundToInt() ?: "N/A"} ms",
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "Jitter: ${item.jitter?.roundToInt() ?: "N/A"} ms",
                        fontSize = 14.sp,
                    )
                }
                Column(modifier = Modifier.weight(2f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "DOWNLOAD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${item.downloadSpeed?.roundToInt() ?: "N/A"} Mbps",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(modifier = Modifier.width(100.dp).height(60.dp)) {
                            LinearChart(
                                speeds = item.downloadSpeeds,
                                lineColor = Color.Blue,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "UPLOAD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${item.uploadSpeed?.roundToInt() ?: "N/A"} Mbps",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Box(modifier = Modifier.width(100.dp).height(60.dp)) {
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
}

private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.date} ${localDateTime.time.hour}:${localDateTime.time.minute.toString().padStart(2, '0')}"
}
