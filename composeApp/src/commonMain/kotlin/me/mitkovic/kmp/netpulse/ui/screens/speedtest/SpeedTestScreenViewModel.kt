package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger
import kotlin.coroutines.cancellation.CancellationException

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
    private var speedTestJob: Job? = null

    private val _databaseUiState = MutableStateFlow<DatabaseUiState>(DatabaseUiState.Loading)
    val databaseUiState: StateFlow<DatabaseUiState> = _databaseUiState.asStateFlow()

    private val _serverUiState = MutableStateFlow<ServerUiState>(ServerUiState.Loading)
    val serverUiState: StateFlow<ServerUiState> = _serverUiState.asStateFlow()

    private val _progress = MutableStateFlow(SpeedTestProgress())
    val progress: StateFlow<SpeedTestProgress> = _progress.asStateFlow()

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
                    logger.logError(
                        SpeedTestScreenViewModel::class.simpleName,
                        "Speed test on server: $server",
                        null,
                    )
                    ServerUiState.Success(server)
                } else {
                    ServerUiState.Error("Server not found for ID: $serverId")
                }
        }
    }

    // SpeedTestScreenViewModel - startSpeedTest method
    fun startSpeedTest(server: Server) {
        speedTestJob =
            viewModelScope.launch {
                _databaseUiState.value = DatabaseUiState.Loading
                try {
                    appRepository.speedTestRepository.executeSpeedTest(server).collect { prog ->
                        if (prog.ping != null) _progress.value = _progress.value.copy(ping = prog.ping)
                        if (prog.jitter != null) _progress.value = _progress.value.copy(jitter = prog.jitter)
                        if (prog.packetLoss != null) _progress.value = _progress.value.copy(packetLoss = prog.packetLoss)
                        if (prog.downloadSpeed != null) _progress.value = _progress.value.copy(downloadSpeed = prog.downloadSpeed)
                        if (prog.uploadSpeed != null) _progress.value = _progress.value.copy(uploadSpeed = prog.uploadSpeed)
                        if (prog.isCompleted) _databaseUiState.value = DatabaseUiState.Completed
                        if (prog.error != null) _databaseUiState.value = DatabaseUiState.Error(prog.error)
                    }
                } catch (e: CancellationException) {
                    logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Speed test cancelled: ${e.message}")
                }
            }
    }

    fun stopSpeedTest() {
        speedTestJob?.cancel()
        logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Speed test cancelled")
        reset()
    }

    override fun onCleared() {
        super.onCleared()
        speedTestJob?.cancel()
    }

    fun reset() {
        _databaseUiState.value = DatabaseUiState.Loading
        _progress.value = SpeedTestProgress()
    }
}
