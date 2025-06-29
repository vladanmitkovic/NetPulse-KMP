package me.mitkovic.kmp.netpulse.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
        val servers: List<Server> = emptyList(),
    ) : SpeedTestServersUiState()
}

sealed class NearestServerUiState {
    object Loading : NearestServerUiState()

    data class Success(
        val nearestServer: Server?,
    ) : NearestServerUiState()

    data class Error(
        val error: String,
    ) : NearestServerUiState()
}

class HomeScreenViewModel(
    private val netPulseRepository: NetPulseRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(HomeScreenViewModel::class.simpleName, "HomeScreenViewModel", null)
    }

    val serverFlow: StateFlow<SpeedTestServersUiState> =
        netPulseRepository
            .speedTestServersRepository
            .getSpeedTestServers()
            .onStart { emit(Resource.Loading) }
            .catch { e ->
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
                    is Resource.Loading -> SpeedTestServersUiState.Loading
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SpeedTestServersUiState.Success(emptyList()),
            )

    private val _nearestServerState = MutableStateFlow<NearestServerUiState>(NearestServerUiState.Success(null))
    val nearestServerUiState: StateFlow<NearestServerUiState> = _nearestServerState.asStateFlow()

    fun findNearestServer() {
        logger.logDebug(HomeScreenViewModel::class.simpleName, "findNearestServer")
        viewModelScope.launch {
            try {
                _nearestServerState.value = NearestServerUiState.Loading
                val nearestServer = netPulseRepository.speedTestServersRepository.findNearestServer()
                _nearestServerState.value =
                    if (nearestServer != null) {
                        NearestServerUiState.Success(nearestServer)
                    } else {
                        NearestServerUiState.Error("No server found")
                    }
                logger.logDebug(
                    HomeScreenViewModel::class.simpleName,
                    "HomeScreenViewModel Nearest server found: ${nearestServer?.name ?: "None"}",
                )
            } catch (e: Exception) {
                logger.logError(
                    HomeScreenViewModel::class.simpleName,
                    "Error finding nearest server: ${e.message}",
                    e,
                )
                _nearestServerState.value = NearestServerUiState.Error(e.message ?: SOMETHING_WENT_WRONG)
            }
        }
    }

    fun logDebug(message: String) {
        logger.logError(HomeScreenViewModel::class.simpleName, message, null)
    }
}
