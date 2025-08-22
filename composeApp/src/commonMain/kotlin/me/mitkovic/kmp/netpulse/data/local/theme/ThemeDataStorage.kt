package me.mitkovic.kmp.netpulse.data.local.theme

import kotlinx.coroutines.flow.Flow

interface ThemeDataStorage {

    suspend fun saveTheme(isLightMode: Boolean)

    fun getTheme(): Flow<Boolean>
}
