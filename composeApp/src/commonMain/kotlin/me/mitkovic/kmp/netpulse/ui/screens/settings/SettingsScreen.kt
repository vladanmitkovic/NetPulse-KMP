package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsScreenViewModel) {
    val testDuration by viewModel.testDuration.collectAsStateWithLifecycle(10)
    val numberOfPings by viewModel.numberOfPings.collectAsStateWithLifecycle(10)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Test Duration Slider
        Text(
            text = "Test Duration: $testDuration s",
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = testDuration.toFloat(),
            onValueChange = { viewModel.saveTestDuration(it.toInt()) },
            valueRange = 5f..20f,
            steps = 5,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Number of Pings Slider
        Text(
            text = "Number of Pings: $numberOfPings",
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = numberOfPings.toFloat(),
            onValueChange = { viewModel.saveNumberOfPings(it.toInt()) },
            valueRange = 5f..20f,
            steps = 5,
        )
    }
}
