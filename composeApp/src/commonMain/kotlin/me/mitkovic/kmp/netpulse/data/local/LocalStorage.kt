package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.location.LocationStorage
import me.mitkovic.kmp.netpulse.data.local.server.ServerStorage
import me.mitkovic.kmp.netpulse.data.local.settings.SettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage
import me.mitkovic.kmp.netpulse.data.local.theme.ThemeDataStorage

interface LocalStorage {
    val locationStorage: LocationStorage
    val serverStorage: ServerStorage
    val testResultStorage: TestResultStorage
    val themeDataStorage: ThemeDataStorage
    val settingsDataStorage: SettingsDataStorage
}
