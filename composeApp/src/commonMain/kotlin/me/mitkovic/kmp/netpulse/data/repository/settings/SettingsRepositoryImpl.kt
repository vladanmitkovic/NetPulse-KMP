package me.mitkovic.kmp.netpulse.data.repository.settings

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.ILocalStorage

class SettingsRepositoryImpl(
    private val localStorage: ILocalStorage,
) : ISettingsRepository {

    override suspend fun saveTestDuration(seconds: Int) {
        localStorage.settingsDataStorage.saveTestDuration(seconds)
    }

    override fun getTestDuration(): Flow<Int> = localStorage.settingsDataStorage.getTestDuration()

    override suspend fun saveNumberOfPings(count: Int) {
        localStorage.settingsDataStorage.saveNumberOfPings(count)
    }

    override fun getNumberOfPings(): Flow<Int> = localStorage.settingsDataStorage.getNumberOfPings()
}
