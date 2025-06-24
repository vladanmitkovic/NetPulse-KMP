package me.mitkovic.kmp.netpulse.data.local.speedtestservers

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.model.Server
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse

open class SpeedTestServersDataSourceImpl(
    private val database: NetPulseDatabase,
) : SpeedTestServersDataSource {

    override suspend fun saveSpeedTestServers(response: SpeedTestServersResponse) {
        database.netPulseDatabaseQueries.transaction {
            // Clear existing data
            database.netPulseDatabaseQueries.clearSpeedTestServers()
            // Insert new server entries
            response.servers.forEach { server ->
                database.netPulseDatabaseQueries.insertSpeedTestServer(
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

    override fun getSpeedTestServers(): Flow<SpeedTestServersResponse?> =
        database.netPulseDatabaseQueries
            .getSpeedTestServers()
            .asFlow()
            .mapToList(context = Dispatchers.Default)
            .map { entities ->
                if (entities.isEmpty()) {
                    null
                } else {
                    SpeedTestServersResponse(
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

    override suspend fun clearSpeedTestServers() {
        database.netPulseDatabaseQueries.clearSpeedTestServers()
    }
}
