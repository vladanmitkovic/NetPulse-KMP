package me.mitkovic.kmp.netpulse.data.local.server

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.ServersResponse

interface ServerStorage {
    suspend fun storeServers(response: ServersResponse)

    fun retrieveServers(): Flow<ServersResponse?>

    fun getServer(serverId: Int): Flow<ServersResponse?>

    suspend fun clearServers()
}
