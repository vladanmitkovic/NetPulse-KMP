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
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger

sealed class ServersUiState {
    object Loading : ServersUiState()

    data class Success(
        val servers: List<Server>,
    ) : ServersUiState()

    data class Error(
        val error: String,
        val servers: List<Server> = emptyList(),
    ) : ServersUiState()
}

sealed class NearestServerByLocationUiState {
    object Loading : NearestServerByLocationUiState()

    data class Success(
        val nearestServer: Server?,
    ) : NearestServerByLocationUiState()

    data class Error(
        val error: String,
    ) : NearestServerByLocationUiState()
}

/*
sealed class NearestServerUiState {
    object Loading : NearestServerUiState()

    data class Success(
        val nearestServer: Server?,
    ) : NearestServerUiState()

    data class Error(
        val error: String,
    ) : NearestServerUiState()
}
*/

class HomeScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    init {
        logger.logError(HomeScreenViewModel::class.simpleName, "HomeScreenViewModel", null)
    }

    private val _nearestServerByLocationState = MutableStateFlow<NearestServerByLocationUiState>(NearestServerByLocationUiState.Loading)
    val nearestServerByLocationUiState: StateFlow<NearestServerByLocationUiState> = _nearestServerByLocationState.asStateFlow()

    private val _sortedServersState = MutableStateFlow<List<Server>>(emptyList())
    val sortedServersUiState: StateFlow<List<Server>> = _sortedServersState.asStateFlow()

    val serverFlow: StateFlow<ServersUiState> =
        appRepository
            .speedTestRepository
            .getServers()
            .onStart { emit(Resource.Loading) }
            .catch { e ->
                logger.logError(HomeScreenViewModel::class.simpleName, "Error fetching servers", e)
                emit(Resource.Error(e.message ?: SOMETHING_WENT_WRONG))
            }.map { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data?.servers?.isNotEmpty() == true) {
                            // findNearestServer()
                            findNearestServerByLocation()
                            loadSortedServersByDistance()
                        }
                        ServersUiState.Success(servers = resource.data?.servers ?: emptyList())
                    }
                    is Resource.Error -> ServersUiState.Error(error = resource.message)
                    is Resource.Loading -> ServersUiState.Loading
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ServersUiState.Loading,
            )

    private fun findNearestServerByLocation() {
        logger.logDebug(HomeScreenViewModel::class.simpleName, "findNearestServerByLocation")
        viewModelScope.launch {
            try {
                _nearestServerByLocationState.value = NearestServerByLocationUiState.Loading
                val nearestServer = appRepository.speedTestRepository.findClosestServerByDistance()
                _nearestServerByLocationState.value =
                    if (nearestServer != null) {
                        NearestServerByLocationUiState.Success(nearestServer)
                    } else {
                        NearestServerByLocationUiState.Error("No server found")
                    }
                logger.logDebug(
                    HomeScreenViewModel::class.simpleName,
                    "Nearest server by location found: ${nearestServer ?: "None"}",
                )
            } catch (e: Exception) {
                logger.logError(
                    HomeScreenViewModel::class.simpleName,
                    "Error finding nearest server by location: ${e.message}",
                    e,
                )
                _nearestServerByLocationState.value = NearestServerByLocationUiState.Error(e.message ?: SOMETHING_WENT_WRONG)
            }
        }
    }

    fun selectServer(server: Server) {
        _nearestServerByLocationState.value = NearestServerByLocationUiState.Success(server)
    }

    private fun loadSortedServersByDistance() {
        viewModelScope.launch {
            try {
                val sortedServers = appRepository.speedTestRepository.getSortedServersByDistance()
                _sortedServersState.value = sortedServers

                logger.logDebug(
                    HomeScreenViewModel::class.simpleName,
                    "SortedServers location: ${sortedServers ?: "None"}",
                )
            } catch (e: Exception) {
                logger.logError(
                    HomeScreenViewModel::class.simpleName,
                    "Error loading sorted servers: ${e.message}",
                    e,
                )
            }
        }
    }

    fun logDebug(message: String) {
        logger.logError(HomeScreenViewModel::class.simpleName, message, null)
    }

    /*
    private val _nearestServerState = MutableStateFlow<NearestServerUiState>(NearestServerUiState.Loading)
    val nearestServerUiState: StateFlow<NearestServerUiState> = _nearestServerState.asStateFlow()

    private fun findNearestServer() {
        logger.logDebug(HomeScreenViewModel::class.simpleName, "findNearestServer")
        viewModelScope.launch {
            try {
                _nearestServerState.value = NearestServerUiState.Loading
                val nearestServer = appRepository.speedTestRepository.findLowestLatencyServer()
                _nearestServerState.value =
                    if (nearestServer != null) {
                        NearestServerUiState.Success(nearestServer)
                    } else {
                        NearestServerUiState.Error("No server found")
                    }
                logger.logDebug(
                    HomeScreenViewModel::class.simpleName,
                    "Nearest server found: ${nearestServer ?: "None"}",
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
     */
}
