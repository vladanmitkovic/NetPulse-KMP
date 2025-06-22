package me.mitkovic.kmp.netpulse.data.remote

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse

interface RemoteDataSource {

    suspend fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse>>
}
