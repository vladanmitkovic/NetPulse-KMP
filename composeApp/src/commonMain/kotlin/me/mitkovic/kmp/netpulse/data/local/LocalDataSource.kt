package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.speedtestresults.SpeedTestResultsDataSource
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSource

interface LocalDataSource {
    val speedTestServers: SpeedTestServersDataSource
    val speedTestResults: SpeedTestResultsDataSource
}
