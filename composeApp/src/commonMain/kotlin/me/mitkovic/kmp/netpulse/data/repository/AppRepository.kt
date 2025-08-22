package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.ThemeRepository
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository

interface AppRepository {
    val speedTestRepository: SpeedTestRepository
    val themeRepository: ThemeRepository
    val settingsRepository: SettingsRepository
}
