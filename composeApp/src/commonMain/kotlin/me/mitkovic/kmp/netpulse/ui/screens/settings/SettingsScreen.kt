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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.mitkovic.kmp.netpulse.ui.theme.spacing

@Composable
fun SettingsScreen(viewModel: SettingsScreenViewModel) {
    val settingsUi by viewModel.settingsUi.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.medium)) {
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
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

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
