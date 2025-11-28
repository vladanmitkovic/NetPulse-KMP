package me.mitkovic.kmp.netpulse.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.logging.IAppLogger

data class SettingsUi(
    val testDurationLabel: String = "Test Duration: 10 s",
    val testDurationValue: Float = 10f,
    val numberOfPingsLabel: String = "Number of Pings: 10",
    val numberOfPingsValue: Float = 10f,
)

class SettingsScreenViewModel(
    private val appRepository: IAppRepository,
    logger: IAppLogger,
) : ViewModel() {

    init {
        logger.logError(SettingsScreenViewModel::class.simpleName, "SettingsScreenViewModel", null)
    }

    private val testDuration = appRepository.settingsRepository.getTestDuration()
    private val numberOfPings = appRepository.settingsRepository.getNumberOfPings()

    val settingsUi: StateFlow<SettingsUi> =
        combine(testDuration, numberOfPings) { duration, pings ->
            SettingsUi(
                testDurationLabel = "Test Duration: $duration s",
                testDurationValue = duration.toFloat(),
                numberOfPingsLabel = "Number of Pings: $pings",
                numberOfPingsValue = pings.toFloat(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUi(),
        )

    fun onTestDurationChanged(value: Float) {
        viewModelScope.launch {
            appRepository.settingsRepository.saveTestDuration(value.toInt())
        }
    }

    fun onNumberOfPingsChanged(value: Float) {
        viewModelScope.launch {
            appRepository.settingsRepository.saveNumberOfPings(value.toInt())
        }
    }

    fun saveTestDuration(seconds: Int) {
        viewModelScope.launch { appRepository.settingsRepository.saveTestDuration(seconds) }
    }

    fun saveNumberOfPings(count: Int) {
        viewModelScope.launch { appRepository.settingsRepository.saveNumberOfPings(count) }
    }
}
