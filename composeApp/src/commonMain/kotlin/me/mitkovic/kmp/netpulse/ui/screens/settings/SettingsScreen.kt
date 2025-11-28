package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsScreenViewModel) {
    val settingsUi by viewModel.settingsUi.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        // Test Duration Slider
        Text(
            text = settingsUi.testDurationLabel,
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = settingsUi.testDurationValue,
            onValueChange = viewModel::onTestDurationChanged,
            valueRange = 5f..20f,
            steps = 5,
            colors =
                SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                    activeTickColor = MaterialTheme.colorScheme.secondary,
                    inactiveTickColor = MaterialTheme.colorScheme.primary,
                ),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Number of Pings Slider
        Text(
            text = settingsUi.numberOfPingsLabel,
            style = MaterialTheme.typography.labelMedium,
        )
        Slider(
            value = settingsUi.numberOfPingsValue,
            onValueChange = viewModel::onNumberOfPingsChanged,
            valueRange = 5f..20f,
            steps = 5,
            colors =
                SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                    activeTickColor = MaterialTheme.colorScheme.secondary,
                    inactiveTickColor = MaterialTheme.colorScheme.primary,
                ),
        )
    }
}
