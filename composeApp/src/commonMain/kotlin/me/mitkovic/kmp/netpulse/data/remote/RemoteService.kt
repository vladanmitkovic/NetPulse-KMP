package me.mitkovic.kmp.netpulse.data.remote

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.local.LocalStorage
import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.data.model.PingResult
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.Server

interface RemoteService {
    suspend fun fetchSpeedTestServers(): Flow<Resource<ServersResponse>>

    suspend fun findNearestServer(servers: List<Server>): Server?

    suspend fun getUserLocation(): GeoIpResponse?

    suspend fun measurePingAndJitter(server: Server): PingResult

    suspend fun performSpeedTest(
        server: Server,
        localStorage: LocalStorage,
    )

    suspend fun downloadTestMultiThread(
        server: Server,
        initialImageSize: String,
        timeout: Double,
        onResult: suspend (Double) -> Unit,
    )

    suspend fun uploadTestMultiThread(
        server: Server,
        initialPayloadSize: Int,
        timeout: Double,
        onResult: suspend (Double) -> Unit,
    )
}
