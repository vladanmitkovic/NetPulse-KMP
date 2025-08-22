package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.data.repository.settings.SettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.ThemeRepository
import me.mitkovic.kmp.netpulse.domain.repository.SpeedTestRepository

class AppRepositoryImpl(
    override val speedTestRepository: SpeedTestRepository,
    override val themeRepository: ThemeRepository,
    override val settingsRepository: SettingsRepository,
) : AppRepository
