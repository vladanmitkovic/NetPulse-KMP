package me.mitkovic.kmp.netpulse.data.repository.theme

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.ILocalStorage

class ThemeRepositoryImpl(
    private val localStorage: ILocalStorage,
) : IThemeRepository {

    override suspend fun saveTheme(isLightMode: Boolean) {
        localStorage.themeDataStorage.saveTheme(isLightMode = isLightMode)
    }

    override fun getTheme(): Flow<Boolean> = localStorage.themeDataStorage.getTheme()
}
