package me.mitkovic.kmp.netpulse.domain.repository

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.domain.model.SpeedTestServersResponse

interface SpeedTestServersRepository {

    fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>>

    fun refreshSpeedTestServers(): Flow<Resource<SpeedTestServersResponse?>>
}
