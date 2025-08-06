package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    onBackClick: () -> Unit,
) {
    Text(text = "SettingsScreen")
}
