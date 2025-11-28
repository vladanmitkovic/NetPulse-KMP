package me.mitkovic.kmp.netpulse.data.local.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

class SettingsDataStorageImpl(
    private val defaults: NSUserDefaults,
) : ISettingsDataStorage {

    private val DURATION_KEY = "test_duration"
    private val PINGS_KEY = "number_of_pings"

    private val durationFlow =
        MutableStateFlow(
            if (defaults.objectForKey(DURATION_KEY) ==
                null
            ) {
                10
            } else {
                defaults.integerForKey(DURATION_KEY).toInt()
            },
        )
    private val pingsFlow =
        MutableStateFlow(
            if (defaults.objectForKey(PINGS_KEY) ==
                null
            ) {
                10
            } else {
                defaults.integerForKey(PINGS_KEY).toInt()
            },
        )

    override suspend fun saveTestDuration(seconds: Int) {
        defaults.setInteger(seconds.toLong(), forKey = DURATION_KEY)
        durationFlow.value = seconds
    }

    override fun getTestDuration(): Flow<Int> = durationFlow

    override suspend fun saveNumberOfPings(count: Int) {
        defaults.setInteger(count.toLong(), forKey = PINGS_KEY)
        pingsFlow.value = count
    }

    override fun getNumberOfPings(): Flow<Int> = pingsFlow
}
