package me.mitkovic.kmp.netpulse.data.local.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsDataStorageImpl : SettingsDataStorage {

    private val durationFlow = MutableStateFlow(10)
    private val pingsFlow = MutableStateFlow(10)

    override suspend fun saveTestDuration(seconds: Int) {
        durationFlow.value = seconds
    }

    override fun getTestDuration(): Flow<Int> = durationFlow

    override suspend fun saveNumberOfPings(count: Int) {
        pingsFlow.value = count
    }

    override fun getNumberOfPings(): Flow<Int> = pingsFlow
}
