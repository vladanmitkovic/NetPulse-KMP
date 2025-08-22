package me.mitkovic.kmp.netpulse.data.local.settings

import kotlinx.coroutines.flow.Flow

interface SettingsDataStorage {

    suspend fun saveTestDuration(seconds: Int)

    fun getTestDuration(): Flow<Int>

    suspend fun saveNumberOfPings(count: Int)

    fun getNumberOfPings(): Flow<Int>
}
