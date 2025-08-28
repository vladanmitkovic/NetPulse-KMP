package me.mitkovic.kmp.netpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class AppViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(AppViewModel::class.simpleName, "AppViewModel", null)
        viewModelScope.launch {
            appRepository
                .speedTestRepository
                .fetchAndSaveUserLocation()

            appRepository
                .speedTestRepository
                .syncServers()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            logger.logDebug(AppViewModel::class.simpleName, "Success")
                        }
                        is Resource.Error -> {
                            val errorMessage = "${Constants.ERROR_FETCHING_SPEED_TEST_SERVERS}: ${result.message}"
                            result.message.let {
                                logger.logError(AppViewModel::class.simpleName, errorMessage, Exception(it))
                            }
                        }
                        is Resource.Loading -> {
                            logger.logDebug(AppViewModel::class.simpleName, "Loading")
                        }
                    }
                }
        }
    }

    val theme =
        appRepository
            .themeRepository
            .getTheme()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null, // No default theme until loaded
            )

    fun updateTheme(isDarkMode: Boolean) {
        viewModelScope.launch {
            appRepository
                .themeRepository
                .saveTheme(isDarkMode)
        }
    }

    fun toggleTheme() {
        theme.value?.let { updateTheme(!it) }
    }

    fun logMessage(
        message: String,
        throwable: Throwable?,
    ) {
        logger.logError(AppViewModel::class.simpleName, message, throwable)
    }
}
