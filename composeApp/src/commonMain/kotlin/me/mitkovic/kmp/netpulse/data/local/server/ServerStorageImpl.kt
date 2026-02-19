package me.mitkovic.kmp.netpulse.data.local.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.local.database.ServerEntity
import me.mitkovic.kmp.netpulse.data.model.Server
import me.mitkovic.kmp.netpulse.data.model.ServersResponse

open class ServerStorageImpl(
    private val database: NetPulseDatabase,
) : IServerStorage {

    override suspend fun storeServers(response: ServersResponse) {
        val servers =
            response.servers.map { server ->
                ServerEntity(
                    id = server.attrs["id"] ?: error("Missing id"),
                    url = server.attrs["url"] ?: error("Missing url"),
                    lat = server.attrs["lat"]?.toDouble() ?: error("Missing lat"),
                    lon = server.attrs["lon"]?.toDouble() ?: error("Missing lon"),
                    name = server.attrs["name"] ?: error("Missing name"),
                    country = server.attrs["country"] ?: error("Missing country"),
                    cc = server.attrs["cc"] ?: error("Missing cc"),
                    sponsor = server.attrs["sponsor"] ?: error("Missing sponsor"),
                    host = server.attrs["host"] ?: error("Missing host"),
                    distance = server.attrs["distance"]?.toDouble() ?: 0.0,
                )
            }

        database.serverDao().replaceServers(servers)
    }

    override fun retrieveServers(): Flow<ServersResponse?> =
        database.serverDao()
            .observeServers()
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
                                            "distance" to entity.distance.toString(),
                                        ),
                                )
                            },
                    )
                }
            }

    override fun getServer(serverId: Int): Flow<ServersResponse?> =
        database.serverDao()
            .observeServerById(serverId.toString())
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
                                            "distance" to entity.distance.toString(),
                                        ),
                                )
                            },
                    )
                }
            }

    override suspend fun clearServers() {
        database.serverDao().clearServers()
    }
}
