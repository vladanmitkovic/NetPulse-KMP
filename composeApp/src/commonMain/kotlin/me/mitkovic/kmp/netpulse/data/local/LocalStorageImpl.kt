package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.server.ServerStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage

class LocalStorageImpl(
    override val serverStorage: ServerStorage,
    override val testResultStorage: TestResultStorage,
) : LocalStorage
