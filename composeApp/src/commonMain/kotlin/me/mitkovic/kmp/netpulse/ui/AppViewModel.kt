package me.mitkovic.kmp.netpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepository
import me.mitkovic.kmp.netpulse.logging.AppLogger

class AppViewModel(
    private val netPulseRepository: NetPulseRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(AppViewModel::class.simpleName, "AppViewModel", null)
        fetchSpeedTestServers()
    }

    fun fetchSpeedTestServers() {
        viewModelScope.launch {
            netPulseRepository
                .speedTestServersRepository
                .refreshSpeedTestServers()
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
}
