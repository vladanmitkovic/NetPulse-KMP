package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.location.ILocationStorage
import me.mitkovic.kmp.netpulse.data.local.server.IServerStorage
import me.mitkovic.kmp.netpulse.data.local.settings.ISettingsDataStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.ITestResultStorage
import me.mitkovic.kmp.netpulse.data.local.theme.IThemeDataStorage

class LocalStorageImpl(
    override val locationStorage: ILocationStorage,
    override val serverStorage: IServerStorage,
    override val testResultStorage: ITestResultStorage,
    override val themeDataStorage: IThemeDataStorage,
    override val settingsDataStorage: ISettingsDataStorage,
) : ILocalStorage
