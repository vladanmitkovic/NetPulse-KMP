package me.mitkovic.kmp.netpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.common.Constants.SOMETHING_WENT_WRONG
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.repository.NetPulseRepository
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger

sealed class SpeedTestServersUiState {
    object Loading : SpeedTestServersUiState()

    data class Success(
        val servers: List<Server>,
    ) : SpeedTestServersUiState()

    data class Error(
        val error: String,
    ) : SpeedTestServersUiState()
}

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

    val conversionRatesUiState: StateFlow<SpeedTestServersUiState> =
        netPulseRepository
            .speedTestServersRepository
            .getSpeedTestServers()
            .onStart {
                emit(Resource.Loading)
            }.catch { e ->
                logger.logError(AppViewModel::class.simpleName, "Error fetching rates", e)
                emit(Resource.Error(e.message ?: SOMETHING_WENT_WRONG))
            }.map { resource ->
                when (resource) {
                    is Resource.Success ->
                        SpeedTestServersUiState.Success(
                            servers = resource.data?.servers ?: emptyList(),
                        )
                    is Resource.Error ->
                        SpeedTestServersUiState.Error(
                            error = resource.message,
                        )
                    is Resource.Loading ->
                        SpeedTestServersUiState.Loading
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SpeedTestServersUiState.Success(emptyList()),
            )
}
