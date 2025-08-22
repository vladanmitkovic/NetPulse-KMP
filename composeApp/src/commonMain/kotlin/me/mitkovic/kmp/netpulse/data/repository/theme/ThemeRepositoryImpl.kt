package me.mitkovic.kmp.netpulse.data.repository.theme

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.LocalStorage

class ThemeRepositoryImpl(
    private val localStorage: LocalStorage,
) : ThemeRepository {

    override suspend fun saveTheme(isDarkMode: Boolean) {
        localStorage.themeDataStorage.saveTheme(isDarkMode)
    }

    override fun getTheme(): Flow<Boolean> = localStorage.themeDataStorage.getTheme()
}
