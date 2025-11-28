package me.mitkovic.kmp.netpulse.data.local.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStorageImpl(
    private val dataStore: DataStore<Preferences>,
) : ISettingsDataStorage {
    companion object {
        private val DURATION_KEY = intPreferencesKey("test_duration")
        private val PINGS_KEY = intPreferencesKey("number_of_pings")
    }

    override suspend fun saveTestDuration(seconds: Int) {
        dataStore.edit { prefs -> prefs[DURATION_KEY] = seconds }
    }

    override fun getTestDuration(): Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[DURATION_KEY] ?: 10
        }

    override suspend fun saveNumberOfPings(count: Int) {
        dataStore.edit { prefs -> prefs[PINGS_KEY] = count }
    }

    override fun getNumberOfPings(): Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[PINGS_KEY] ?: 10
        }
}
