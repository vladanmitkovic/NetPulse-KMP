package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.location.ILocationStorage
import me.mitkovic.kmp.netpulse.data.local.server.IServerStorage
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.ITestResultStorage
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage

interface ILocalStorage {
    val locationStorage: ILocationStorage
    val serverStorage: IServerStorage
    val testResultStorage: ITestResultStorage
    val themeDataStorage: IThemeDataStorage
    val settingsDataStorage: ISettingsDataStorage
}
