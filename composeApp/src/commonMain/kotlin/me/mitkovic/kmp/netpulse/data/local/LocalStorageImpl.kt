package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.location.LocationStorage
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorage

class LocalStorageImpl(
    override val locationStorage: LocationStorage,
    override val serverStorage: ServerStorage,
    override val testResultStorage: TestResultStorage,
    override val themeDataStorage: ThemeDataStorage,
    override val settingsDataStorage: SettingsDataStorage,
) : LocalStorage
