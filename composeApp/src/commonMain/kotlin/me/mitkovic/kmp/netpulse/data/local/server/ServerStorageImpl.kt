package me.mitkovic.kmp.netpulse.data.local.server

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.model.Server
import me.mitkovic.kmp.netpulse.data.model.ServersResponse

open class ServerStorageImpl(
    private val database: NetPulseDatabase,
) : ServerStorage {

    override suspend fun storeServers(response: ServersResponse) {
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.clearServers()
            response.servers.forEach { server ->
                database.netPulseDatabaseQueries.insertServer(
                    id = server.attrs["id"] ?: error("Missing id"),
                    url = server.attrs["url"] ?: error("Missing url"),
                    lat = server.attrs["lat"]?.toDouble() ?: error("Missing lat"),
                    lon = server.attrs["lon"]?.toDouble() ?: error("Missing lon"),
                    name = server.attrs["name"] ?: error("Missing name"),
                    country = server.attrs["country"] ?: error("Missing country"),
                    cc = server.attrs["cc"] ?: error("Missing cc"),
                    sponsor = server.attrs["sponsor"] ?: error("Missing sponsor"),
                    host = server.attrs["host"] ?: error("Missing host"),
                )
            }
        }
    }

    override fun retrieveServers(): Flow<ServersResponse?> =
        database.netPulseDatabaseQueries
            .getServers()
            .asFlow()
            .mapToList(context = Dispatchers.Default)
            .map { entities ->
                if (entities.isEmpty()) {
                    null
                } else {
                    ServersResponse(
                        servers =
                            entities.map { entity ->
                                Server(
                                    attrs =
                                        mapOf(
                                            "id" to entity.id,
                                            "url" to entity.url,
                                            "lat" to entity.lat.toString(),
                                            "lon" to entity.lon.toString(),
                                            "name" to entity.name,
                                            "country" to entity.country,
                                            "cc" to entity.cc,
                                            "sponsor" to entity.sponsor,
                                            "host" to entity.host,
                                        ),
                                )
                            },
                    )
                }
            }

    override fun getServer(serverId: Int): Flow<ServersResponse?> =
        database.netPulseDatabaseQueries
            .getServerById(serverId.toString())
            .asFlow()
            .mapToList(context = Dispatchers.Default)
            .map { entities ->
                if (entities.isEmpty()) {
                    null
                } else {
                    ServersResponse(
                        servers =
                            entities.map { entity ->
                                Server(
                                    attrs =
                                        mapOf(
                                            "id" to entity.id,
                                            "url" to entity.url,
                                            "lat" to entity.lat.toString(),
                                            "lon" to entity.lon.toString(),
                                            "name" to entity.name,
                                            "country" to entity.country,
                                            "cc" to entity.cc,
                                            "sponsor" to entity.sponsor,
                                            "host" to entity.host,
                                        ),
                                )
                            },
                    )
                }
            }

    override suspend fun clearServers() {
        database.netPulseDatabaseQueries.clearServers()
    }
}
