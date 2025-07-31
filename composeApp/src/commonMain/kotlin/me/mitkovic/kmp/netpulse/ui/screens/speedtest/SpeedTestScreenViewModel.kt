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
import me.mitkovic.kmp.netpulse.data.local.database.TestSession
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
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

sealed class SessionUiState {
    object Loading : SessionUiState()

    data class Success(
        val session: TestSession?,
    ) : SessionUiState()

    data class Error(
        val error: String,
    ) : SessionUiState()
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

    private val _ping = MutableStateFlow<Double?>(null)
    val ping: StateFlow<Double?> = _ping.asStateFlow()

    private val _jitter = MutableStateFlow<Double?>(null)
    val jitter: StateFlow<Double?> = _jitter.asStateFlow()

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

            sessionFlow.collect { uiState ->
                if (uiState is SessionUiState.Success) {
                    val session = uiState.session ?: return@collect
                    _ping.value = session.ping
                    _jitter.value = session.jitter
                }
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

    val sessionFlow: StateFlow<SessionUiState> =
        appRepository.speedTestRepository
            .observeLatestTestSession(serverId)
            .map { resource ->
                when (resource) {
                    is Resource.Success -> SessionUiState.Success(resource.data)
                    is Resource.Error -> SessionUiState.Error(resource.message)
                    is Resource.Loading -> SessionUiState.Loading
                }
            }.catch { e ->
                logger.logError(
                    SpeedTestScreenViewModel::class.simpleName,
                    "Error observing speed test session: ${e.message}",
                    e,
                )
                emit(SessionUiState.Error(e.message ?: SOMETHING_WENT_WRONG))
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SessionUiState.Loading,
            )

    /*
    fun startSpeedTest(server: Server) {
        viewModelScope.launch {
            logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Starting speed test for server: ${server.name}")
            try {
                _databaseUiState.value = DatabaseUiState.Loading
                _ping.value = null
                _jitter.value = null
                appRepository.speedTestRepository.executeSpeedTest(server)
                logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Speed test completed for server: ${server.name}")
                _databaseUiState.value = DatabaseUiState.Completed
            } catch (e: Exception) {
                logger.logError(SpeedTestScreenViewModel::class.simpleName, "Speed test failed: ${e.message}", e)
                _databaseUiState.value = DatabaseUiState.Error(e.message ?: SOMETHING_WENT_WRONG)
            }
        }
    }
     */

    fun startSpeedTest(server: Server) {
        viewModelScope.launch {
            _databaseUiState.value = DatabaseUiState.Loading
            appRepository.speedTestRepository.executeSpeedTest(server).collect { prog ->
                if (prog.ping != null) _progress.value = _progress.value.copy(ping = prog.ping)
                if (prog.jitter != null) _progress.value = _progress.value.copy(jitter = prog.jitter)
                if (prog.downloadSpeed != null) _progress.value = _progress.value.copy(downloadSpeed = prog.downloadSpeed)
                if (prog.uploadSpeed != null) _progress.value = _progress.value.copy(uploadSpeed = prog.uploadSpeed)
                if (prog.isCompleted) _databaseUiState.value = DatabaseUiState.Completed
                if (prog.error != null) _databaseUiState.value = DatabaseUiState.Error(prog.error)
            }
        }
    }

    fun reset() {
        _databaseUiState.value = DatabaseUiState.Loading
        _progress.value = SpeedTestProgress()
    }
}
