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
import me.mitkovic.kmp.netpulse.data.repository.AppRepository
import me.mitkovic.kmp.netpulse.domain.model.TestHistory
import me.mitkovic.kmp.netpulse.logging.AppLogger

class HistoryScreenViewModel(
    private val appRepository: AppRepository,
    private val logger: AppLogger,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<TestHistory>> =
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

                                    // Logging all download speeds for this session
                                    logger.logDebug(
                                        "HistoryScreenViewModel",
                                        "Session ${session.sessionId} - All download speeds: ${downloadResults.map { it.speed ?: "null" }}",
                                    )
                                    // Logging all upload speeds for this session
                                    logger.logDebug(
                                        "HistoryScreenViewModel",
                                        "Session ${session.sessionId} - All upload speeds: ${uploadResults.map { it.speed ?: "null" }}",
                                    )
                                    // Logging the latest download and upload speeds
                                    logger.logDebug(
                                        "HistoryScreenViewModel",
                                        "Session ${session.sessionId} - Latest download speed: ${download ?: "null"}, Latest upload speed: ${upload ?: "null"}",
                                    )

                                    TestHistory(
                                        sessionId = session.sessionId,
                                        timestamp = session.testTimestamp,
                                        serverName = session.serverName,
                                        serverCountry = session.serverCountry,
                                        ping = session.ping,
                                        jitter = session.jitter,
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
}
