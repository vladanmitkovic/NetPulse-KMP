package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.server.ServerStorage
import me.mitkovic.kmp.netpulse.data.local.testresult.TestResultStorage

interface LocalStorage {
    val serverStorage: ServerStorage
    val testResultStorage: TestResultStorage
}
