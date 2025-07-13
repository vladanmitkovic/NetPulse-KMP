package me.mitkovic.kmp.netpulse.data.remote

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.Server

interface RemoteService {
    suspend fun fetchSpeedTestServers(): Flow<Resource<ServersResponse>>

    suspend fun findNearestServer(servers: List<Server>): Server?

    suspend fun getUserLocation(): GeoIpResponse?

    suspend fun performSpeedTest(
        server: Server,
        localStorage: LocalStorage,
    )
}
