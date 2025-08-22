package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class SettingsScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(SettingsScreenViewModel::class.simpleName, "SettingsScreenViewModel", null)
    }

    val testDuration: Flow<Int> = appRepository.settingsRepository.getTestDuration()
    val numberOfPings: Flow<Int> = appRepository.settingsRepository.getNumberOfPings()

    fun saveTestDuration(seconds: Int) {
        viewModelScope.launch { appRepository.settingsRepository.saveTestDuration(seconds) }
    }

    fun saveNumberOfPings(count: Int) {
        viewModelScope.launch { appRepository.settingsRepository.saveNumberOfPings(count) }
    }
}
