package me.mitkovic.kmp.netpulse.data.repository.theme

import kotlinx.coroutines.flow.Flow

interface IThemeRepository {

    suspend fun saveTheme(isLightMode: Boolean)

    fun getTheme(): Flow<Boolean>
}
