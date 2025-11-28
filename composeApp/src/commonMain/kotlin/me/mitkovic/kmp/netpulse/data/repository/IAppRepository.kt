package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.data.repository.settings.ISettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.IThemeRepository
import me.mitkovic.kmp.netpulse.domain.repository.ISpeedTestRepository

interface IAppRepository {
    val speedTestRepository: ISpeedTestRepository
    val themeRepository: IThemeRepository
    val settingsRepository: ISettingsRepository
}
