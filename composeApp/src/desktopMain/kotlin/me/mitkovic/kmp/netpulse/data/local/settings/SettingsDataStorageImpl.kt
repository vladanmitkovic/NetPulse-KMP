package me.mitkovic.kmp.netpulse.data.local.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.prefs.Preferences

class SettingsDataStorageImpl : ISettingsDataStorage {

    private val prefs = Preferences.userNodeForPackage(SettingsDataStorageImpl::class.java)

    companion object {
        private const val DURATION_KEY = "test_duration"
        private const val PINGS_KEY = "number_of_pings"
        private const val DEFAULT_DURATION = 10
        private const val DEFAULT_PINGS = 10
    }

    private val durationFlow = MutableStateFlow(prefs.getInt(DURATION_KEY, DEFAULT_DURATION))
    private val pingsFlow = MutableStateFlow(prefs.getInt(PINGS_KEY, DEFAULT_PINGS))

    override suspend fun saveTestDuration(seconds: Int) {
        prefs.putInt(DURATION_KEY, seconds)
        prefs.flush()
        durationFlow.value = seconds
    }

    override fun getTestDuration(): Flow<Int> = durationFlow

    override suspend fun saveNumberOfPings(count: Int) {
        prefs.putInt(PINGS_KEY, count)
        prefs.flush()
        pingsFlow.value = count
    }

    override fun getNumberOfPings(): Flow<Int> = pingsFlow
}
