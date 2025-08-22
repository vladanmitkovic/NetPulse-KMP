package me.mitkovic.kmp.netpulse.data.local.theme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ThemeDataStorageImpl : ThemeDataStorage {

    private val isLightModeFlow = MutableStateFlow(false)

    override suspend fun saveTheme(isLightMode: Boolean) {
        isLightModeFlow.value = isLightMode
    }

    override fun getTheme(): Flow<Boolean> = isLightModeFlow
}
