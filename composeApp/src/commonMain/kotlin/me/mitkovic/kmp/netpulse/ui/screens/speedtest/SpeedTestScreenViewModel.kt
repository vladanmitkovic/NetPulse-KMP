package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.common.Constants.SOMETHING_WENT_WRONG
import me.mitkovic.kmp.netpulse.data.local.database.TestResult
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger

sealed class ServerUiState {
    object Loading : ServerUiState()

    data class Success(
        val server: Server?,
    ) : ServerUiState()

    data class Error(
        val error: String,
    ) : ServerUiState()
}

sealed class DatabaseUiState {
    object Loading : DatabaseUiState()

    data class Success(
        val result: TestResult?,
    ) : DatabaseUiState()

    data class Error(
        val error: String,
    ) : DatabaseUiState()

    object Completed : DatabaseUiState()
}

class SpeedTestScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
    serverId: Int,
) : ViewModel() {

    private var hasRunSpeedTest = false

    private val _databaseUiState = MutableStateFlow<DatabaseUiState>(DatabaseUiState.Loading)
    val databaseUiState: StateFlow<DatabaseUiState> = _databaseUiState.asStateFlow()

    private val _serverUiState = MutableStateFlow<ServerUiState>(ServerUiState.Loading)
    val serverUiState: StateFlow<ServerUiState> = _serverUiState.asStateFlow()

    init {
        logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Initialized with serverId: $serverId")
        viewModelScope.launch {
            val server = appRepository.speedTestRepository.getServer(serverId)
            _serverUiState.value =
                if (server != null) {
                    ServerUiState.Success(server)
                    if (!hasRunSpeedTest) {
                        hasRunSpeedTest = true
                        startSpeedTest(server)
                    }
                    ServerUiState.Success(server)
                } else {
                    ServerUiState.Error("Server not found for ID: $serverId")
                }
        }
    }

    val databaseFlow: StateFlow<DatabaseUiState> =
        appRepository.speedTestRepository
            .observeLatestTestResult()
            .map { resource ->
                when (resource) {
                    is Resource.Success -> DatabaseUiState.Success(resource.data)
                    is Resource.Error -> DatabaseUiState.Error(resource.message)
                    is Resource.Loading -> DatabaseUiState.Loading
                }
            }.catch { e ->
                logger.logError(
                    SpeedTestScreenViewModel::class.simpleName,
                    "Error observing speed test result: ${e.message}",
                    e,
                )
                emit(DatabaseUiState.Error(e.message ?: SOMETHING_WENT_WRONG))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DatabaseUiState.Loading,
            )

    fun startSpeedTest(server: Server) {
        viewModelScope.launch {
            logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Starting speed test for server: ${server.name}")
            try {
                appRepository.speedTestRepository.executeSpeedTest(server)
                logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Speed test completed for server: ${server.name}")
                _databaseUiState.value = DatabaseUiState.Completed
            } catch (e: Exception) {
                logger.logError(SpeedTestScreenViewModel::class.simpleName, "Speed test failed: ${e.message}", e)
                _databaseUiState.value = DatabaseUiState.Error(e.message ?: SOMETHING_WENT_WRONG)
            }
        }
    }
}
