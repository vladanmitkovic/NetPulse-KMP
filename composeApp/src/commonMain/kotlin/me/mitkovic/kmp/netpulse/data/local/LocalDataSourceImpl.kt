package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.speedtestresults.SpeedTestResultsDataSource
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSource

class LocalDataSourceImpl(
    override val speedTestResults: SpeedTestResultsDataSource,
    override val speedTestServers: SpeedTestServersDataSource,
) : LocalDataSource
