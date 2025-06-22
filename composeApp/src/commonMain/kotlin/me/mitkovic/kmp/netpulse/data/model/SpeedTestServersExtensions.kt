package me.mitkovic.kmp.netpulse.data.model

import me.mitkovic.kmp.netpulse.domain.model.Server as DomainServer
import me.mitkovic.kmp.netpulse.domain.model.SpeedTestServersResponse as DomainServersResponse

/**
 * Convert the XML‐parsed data model into your domain model.
 */
fun SpeedTestServersResponse.toDomainModel(): DomainServersResponse =
    DomainServersResponse(
        servers = this.servers.map { it.toDomainModel() },
    )

private fun Server.toDomainModel(): DomainServer =
    DomainServer(
        url = attrs["url"] ?: error("Missing url"),
        lat = attrs["lat"] ?.toDouble() ?: error("Missing lat"),
        lon = attrs["lon"] ?.toDouble() ?: error("Missing lon"),
        name = attrs["name"] ?: error("Missing name"),
        country = attrs["country"] ?: error("Missing country"),
        cc = attrs["cc"] ?: error("Missing cc"),
        sponsor = attrs["sponsor"] ?: error("Missing sponsor"),
        id = attrs["id"] ?.toInt() ?: error("Missing id"),
        host = attrs["host"] ?: error("Missing host"),
    )

/**
 * Convert back from your domain model into the XML data model.
 */
fun DomainServersResponse.toDataModel(): SpeedTestServersResponse =
    SpeedTestServersResponse(
        servers = this.servers.map { it.toDataModel() },
    )

private fun DomainServer.toDataModel(): Server =
    Server(
        attrs =
            mapOf(
                "url" to url,
                "lat" to lat.toString(),
                "lon" to lon.toString(),
                "name" to name,
                "country" to country,
                "cc" to cc,
                "sponsor" to sponsor,
                "id" to id.toString(),
                "host" to host,
            ),
    )
