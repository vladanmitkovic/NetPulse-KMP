package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.lifecycle.ViewModel
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SettingsScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(SettingsScreenViewModel::class.simpleName, "SettingsScreenViewModel", null)
    }
}
