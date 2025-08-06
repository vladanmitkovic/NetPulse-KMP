package me.mitkovic.kmp.netpulse.ui.screens.history

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HistoryScreen(
    viewModel: HistoryScreenViewModel,
    onBackClick: () -> Unit,
) {
    Text(text = "HistoryScreen")
}
