package me.mitkovic.kmp.netpulse.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import me.mitkovic.kmp.netpulse.ui.components.LinearChart
import me.mitkovic.kmp.netpulse.ui.theme.spacing

@Composable
fun HistoryScreen(viewModel: HistoryScreenViewModel) {
    val history by viewModel.historyItems.collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "No speed tests performed yet.", color = MaterialTheme.colorScheme.onBackground)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        ) {
            items(
                items = history,
                key = { it.sessionId },
            ) { item ->
                TestHistoryItem(item, onDelete = { viewModel.deleteSession(item.sessionId) })
            }
        }
    }
}

@Composable
fun TestHistoryItem(
    item: HistoryItemUi,
    onDelete: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.small),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.elevation),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
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
                        text = item.formattedTimestamp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium),
                    )
                    Text(
                        text = item.serverSponsor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text =
                            buildAnnotatedString {
                                append(item.serverLocationText)
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(item.formattedDistance)
                                }
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier =
                        Modifier
                            .size(MaterialTheme.spacing.deleteButtonSize)
                            .padding(start = MaterialTheme.spacing.small),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            // Metrics Section
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.medium),
            ) {
                // Row for Ping, Jitter, Packet Loss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MetricItem(label = "Ping", value = item.pingText)
                    MetricItem(label = "Jitter", value = item.jitterText)
                    MetricItem(label = "Packet Loss", value = item.packetLossText)
                }
                // Download part
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(
                            text = "DOWNLOAD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = item.downloadSpeedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(3f)
                                .height(MaterialTheme.spacing.chartHeight),
                    ) {
                        LinearChart(
                            speeds = item.downloadSpeeds,
                            lineColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                // Upload part
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(
                            text = "UPLOAD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = item.uploadSpeedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(3f)
                                .height(MaterialTheme.spacing.chartHeight),
                    ) {
                        LinearChart(
                            speeds = item.uploadSpeeds,
                            lineColor = MaterialTheme.colorScheme.tertiary,
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
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
