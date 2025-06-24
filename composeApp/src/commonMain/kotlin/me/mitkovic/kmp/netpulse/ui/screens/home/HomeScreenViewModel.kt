package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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

class HomeScreenViewModel(
    netPulseRepository: NetPulseRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(HomeScreenViewModel::class.simpleName, "HomeScreenViewModel", null)
    }

    val conversionRatesUiState: StateFlow<SpeedTestServersUiState> =
        netPulseRepository
            .speedTestServersRepository
            .getSpeedTestServers()
            .onStart {
                emit(Resource.Loading)
            }.catch { e ->
                logger.logError(HomeScreenViewModel::class.simpleName, "Error fetching rates", e)
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
                initialValue =
                    SpeedTestServersUiState
                        .Success(emptyList()),
            )

    fun logDebug(message: String) {
        logger.logError(HomeScreenViewModel::class.simpleName, message, null)
    }
}
