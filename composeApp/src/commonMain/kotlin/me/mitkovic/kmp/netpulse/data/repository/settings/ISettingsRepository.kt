package me.mitkovic.kmp.netpulse.data.repository.settings

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {

    suspend fun saveTestDuration(seconds: Int)

    fun getTestDuration(): Flow<Int>

    suspend fun saveNumberOfPings(count: Int)

    fun getNumberOfPings(): Flow<Int>
}
