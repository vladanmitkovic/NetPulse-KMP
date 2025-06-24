package me.mitkovic.kmp.netpulse.data.local

import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSource
import me.mitkovic.kmp.netpulse.data.local.speedtestservers.SpeedTestServersDataSourceImpl

class LocalDataSourceImpl(
    database: NetPulseDatabase,
) : SpeedTestServersDataSourceImpl(database),
    LocalDataSource {

    override val speedTestServers: SpeedTestServersDataSource get() = this
}
