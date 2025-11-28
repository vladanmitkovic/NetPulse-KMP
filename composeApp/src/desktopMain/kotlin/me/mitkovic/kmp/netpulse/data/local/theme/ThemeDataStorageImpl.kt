package me.mitkovic.kmp.netpulse.data.local.theme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.prefs.Preferences

class ThemeDataStorageImpl : IThemeDataStorage {

    private val prefs = Preferences.userNodeForPackage(ThemeDataStorageImpl::class.java)

    companion object {
        private const val THEME_KEY = "is_light_mode"
        private const val DEFAULT_THEME = false
    }

    private val isLightModeFlow = MutableStateFlow(prefs.getBoolean(THEME_KEY, DEFAULT_THEME))

    override suspend fun saveTheme(isLightMode: Boolean) {
        prefs.putBoolean(THEME_KEY, isLightMode)
        prefs.flush() // Ensure it's written to disk
        isLightModeFlow.value = isLightMode
    }

    override fun getTheme(): Flow<Boolean> = isLightModeFlow
}
