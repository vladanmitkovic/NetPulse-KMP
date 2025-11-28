package me.mitkovic.kmp.netpulse.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.mitkovic.kmp.netpulse.data.repository.IAppRepository
import me.mitkovic.kmp.netpulse.domain.model.TestHistory
import me.mitkovic.kmp.netpulse.logging.IAppLogger
import me.mitkovic.kmp.netpulse.util.formatDistanceMetersToKm
import me.mitkovic.kmp.netpulse.util.formatDoubleToInt
import me.mitkovic.kmp.netpulse.util.formatTimestamp

data class HistoryItemUi(
    val sessionId: Long,
    val formattedTimestamp: String,
    val serverSponsor: String,
    val serverLocationText: String,
    val formattedDistance: String,
    val pingText: String,
    val jitterText: String,
    val packetLossText: String,
    val downloadSpeedText: String,
    val uploadSpeedText: String,
    val downloadSpeeds: List<Float>,
    val uploadSpeeds: List<Float>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryScreenViewModel(
    private val appRepository: IAppRepository,
    private val logger: IAppLogger,
) : ViewModel() {

    private val history: StateFlow<List<TestHistory>> =
        appRepository.speedTestRepository
            .getTestSessions()
            .flatMapLatest { sessions ->
                if (sessions.isEmpty()) {
                    logger.logDebug("HistoryScreenViewModel", "No test sessions found")
                    flowOf(emptyList())
                } else {
                    val flows: List<Flow<TestHistory>> =
                        sessions.map { session ->
                            appRepository.speedTestRepository
                                .getTestResultsBySessionId(session.sessionId)
                                .map { results ->
                                    val downloadResults = results.filter { it.testType == 1L }.sortedBy { it.resultTimestamp }
                                    val uploadResults = results.filter { it.testType == 2L }.sortedBy { it.resultTimestamp }
                                    val downloadSpeeds = downloadResults.map { it.speed?.toFloat() ?: 0f }
                                    val uploadSpeeds = uploadResults.map { it.speed?.toFloat() ?: 0f }
                                    val download = downloadResults.maxByOrNull { it.resultTimestamp }?.speed
                                    val upload = uploadResults.maxByOrNull { it.resultTimestamp }?.speed

                                    logger.logDebug("HistoryScreenViewModel", "Session Obj: $session}")
                                    logger.logDebug("HistoryScreenViewModel", "Session ${session.sessionId} - All download speeds: ...")
                                    logger.logDebug("HistoryScreenViewModel", "Session ${session.sessionId} - All upload speeds: ...")
                                    logger.logDebug("HistoryScreenViewModel", "Session ${session.sessionId} - Latest download speed: ...")

                                    TestHistory(
                                        sessionId = session.sessionId,
                                        timestamp = session.testTimestamp,
                                        serverName = session.serverName,
                                        serverCountry = session.serverCountry,
                                        serverSponsor = session.serverSponsor,
                                        serverDistance = session.serverDistance,
                                        ping = session.ping,
                                        jitter = session.jitter,
                                        packetLoss = session.packetLoss,
                                        downloadSpeed = download,
                                        uploadSpeed = upload,
                                        downloadSpeeds = downloadSpeeds,
                                        uploadSpeeds = uploadSpeeds,
                                    )
                                }
                        }
                    if (flows.isEmpty()) {
                        logger.logDebug("HistoryScreenViewModel", "No flows to combine")
                        flowOf(emptyList())
                    } else {
                        combine(flows) { arr -> arr.toList() }
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val historyItems: StateFlow<List<HistoryItemUi>> =
        history
            .map { items ->
                items.map { item -> mapToUi(item) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    private fun mapToUi(item: TestHistory): HistoryItemUi =
        HistoryItemUi(
            sessionId = item.sessionId,
            formattedTimestamp = formatTimestamp(item.timestamp),
            serverSponsor = item.serverSponsor,
            serverLocationText = "${item.serverName} • ${item.serverCountry} • ",
            formattedDistance = formatDistanceMetersToKm(item.serverDistance),
            pingText = "${formatDoubleToInt(item.ping)} ms",
            jitterText = "${formatDoubleToInt(item.jitter)} ms",
            packetLossText = "${formatDoubleToInt(item.packetLoss)} %",
            downloadSpeedText = "${formatDoubleToInt(item.downloadSpeed)} Mbps",
            uploadSpeedText = "${formatDoubleToInt(item.uploadSpeed)} Mbps",
            downloadSpeeds = item.downloadSpeeds,
            uploadSpeeds = item.uploadSpeeds,
        )

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            appRepository.speedTestRepository.deleteTestSession(sessionId)
        }
    }
}
