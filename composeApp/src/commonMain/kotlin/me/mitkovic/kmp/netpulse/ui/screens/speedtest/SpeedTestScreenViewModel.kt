package me.mitkovic.kmp.netpulse.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.data.model.SpeedTestProgress
import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.util.formatDistanceMetersToKm
import me.mitkovic.kmp.netpulse.util.formatDoubleToInt
import kotlin.coroutines.cancellation.CancellationException

sealed class ServerUiState {
    object Loading : ServerUiState()

    data class Success(
        val server: Server?,
    ) : ServerUiState()

    data class Error(
        val errorText: String,
    ) : ServerUiState()
}

sealed class DatabaseUiState {
    object Loading : DatabaseUiState()

    data class Error(
        val errorText: String,
    ) : DatabaseUiState()

    object Completed : DatabaseUiState()
}

data class SpeedTestUi(
    val pingText: String = "0 ms",
    val jitterText: String = "0 ms",
    val packetLossText: String = "0 %",
    val downloadSpeed: Float? = null,
    val uploadSpeed: Float? = null,
    val downloadSpeedText: String = "0",
    val uploadSpeedText: String = "0",
)

data class ServerInfoUi(
    val sponsor: String,
    val locationText: String,
    val formattedDistance: String,
)

class SpeedTestScreenViewModel(
    private val appRepository: IAppRepository,
    private val logger: IAppLogger,
    serverId: Int,
) : ViewModel() {

    private var hasRunSpeedTest = false
    private var speedTestJob: Job? = null

    private val _databaseUiState = MutableStateFlow<DatabaseUiState>(DatabaseUiState.Loading)
    val databaseUiState: StateFlow<DatabaseUiState> = _databaseUiState.asStateFlow()

    private val _serverUiState = MutableStateFlow<ServerUiState>(ServerUiState.Loading)
    val serverUiState: StateFlow<ServerUiState> = _serverUiState.asStateFlow()

    private val _progress = MutableStateFlow(SpeedTestProgress())

    val progressUi: StateFlow<SpeedTestUi> =
        _progress
            .map { progress -> mapToUi(progress) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SpeedTestUi(),
            )

    val serverInfoUi: StateFlow<ServerInfoUi?> =
        _serverUiState
            .map { state ->
                when (state) {
                    is ServerUiState.Success -> state.server?.let { mapServerToUi(it) }
                    else -> null
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    private fun mapToUi(progress: SpeedTestProgress): SpeedTestUi =
        SpeedTestUi(
            pingText = "${formatDoubleToInt(progress.ping)} ms",
            jitterText = "${formatDoubleToInt(progress.jitter)} ms",
            packetLossText = "${formatDoubleToInt(progress.packetLoss)} %",
            downloadSpeed = progress.downloadSpeed?.toFloat(),
            uploadSpeed = progress.uploadSpeed?.toFloat(),
            downloadSpeedText = formatDoubleToInt(progress.downloadSpeed),
            uploadSpeedText = formatDoubleToInt(progress.uploadSpeed),
        )

    private fun mapServerToUi(server: Server): ServerInfoUi =
        ServerInfoUi(
            sponsor = server.sponsor,
            locationText = "${server.name} • ${server.country} • ",
            formattedDistance = formatDistanceMetersToKm(server.distance),
        )

    init {
        logger.logDebug(SpeedTestScreenViewModel::class.simpleName, "Initialized with serverId: $serverId")
        viewModelScope.launch {
            val server = appRepository.speedTestRepository.getServer(serverId)
            if (server != null) {
                _serverUiState.value = ServerUiState.Success(server)
                if (!hasRunSpeedTest) {
                    hasRunSpeedTest = true
                    startSpeedTest(server)
                }
                logger.logDebug(
                    SpeedTestScreenViewModel::class.simpleName,
                    "Speed test on server: $server",
                )
            } else {
                _serverUiState.value =
                    ServerUiState.Error(
                        errorText = "Error: Server not found for ID: $serverId",
                    )
            }
        }
    }

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
                        if (prog.error != null) {
                            _databaseUiState.value =
                                DatabaseUiState.Error(
                                    errorText = "Result Error: ${prog.error}",
                                )
                        }
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
