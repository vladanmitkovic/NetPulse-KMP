package me.mitkovic.kmp.netpulse.data.repository

import me.mitkovic.kmp.netpulse.data.repository.settings.ISettingsRepository
import me.mitkovic.kmp.netpulse.data.repository.theme.IThemeRepository
import me.mitkovic.kmp.netpulse.domain.repository.ISpeedTestRepository

class AppRepositoryImpl(
    override val speedTestRepository: ISpeedTestRepository,
    override val themeRepository: IThemeRepository,
    override val settingsRepository: ISettingsRepository,
) : IAppRepository
