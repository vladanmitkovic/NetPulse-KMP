package me.mitkovic.kmp.netpulse.ui.screens.history

import androidx.lifecycle.ViewModel
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class HistoryScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(HistoryScreenViewModel::class.simpleName, "HistoryScreenViewModel", null)
    }
}
