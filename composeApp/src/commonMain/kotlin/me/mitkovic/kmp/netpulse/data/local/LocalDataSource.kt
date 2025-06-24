package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSource

interface LocalDataSource {
    val speedTestServers: SpeedTestServersDataSource
}
