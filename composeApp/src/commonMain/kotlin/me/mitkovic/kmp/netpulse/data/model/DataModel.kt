package me.mitkovic.kmp.netpulse.data.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import me.mitkovic.kmp.netpulse.domain.model.Server as DomainServer
import me.mitkovic.kmp.netpulse.domain.model.ServersResponse as DomainServersResponse

@Serializable
@XmlSerialName("settings", "", "")
data class ServersResponse(
    @XmlSerialName("servers", "", "")
    @XmlChildrenName("server")
    val servers: List<Server>,
)

@Serializable
@XmlSerialName("server", "", "")
data class Server(
    @XmlOtherAttributes
    val attrs: Map<String, String>,
)

/**
 * Convert the XML‐parsed data model into your domain model.
 */
fun ServersResponse.toDomainModel(): DomainServersResponse =
    DomainServersResponse(
        servers = this.servers.map { it.toDomainModel() },
    )

private fun Server.toDomainModel(): DomainServer =
    DomainServer(
        url = attrs["url"] ?: error("Missing url"),
        lat = attrs["lat"]?.toDouble() ?: error("Missing lat"),
        lon = attrs["lon"]?.toDouble() ?: error("Missing lon"),
        name = attrs["name"] ?: error("Missing name"),
        country = attrs["country"] ?: error("Missing country"),
        cc = attrs["cc"] ?: error("Missing cc"),
        sponsor = attrs["sponsor"] ?: error("Missing sponsor"),
        id = attrs["id"]?.toInt() ?: error("Missing id"),
        host = attrs["host"] ?: error("Missing host"),
        distance = attrs["distance"]?.toDouble(),
    )

fun DomainServersResponse.toDataModel(): ServersResponse =
    ServersResponse(
        servers = this.servers.map { it.toDataModel() },
    )

private fun DomainServer.toDataModel(): Server =
    Server(
        attrs =
            buildMap {
                put("url", url)
                put("lat", lat.toString())
                put("lon", lon.toString())
                put("name", name)
                put("country", country)
                put("cc", cc)
                put("sponsor", sponsor)
                put("id", id.toString())
                put("host", host)
                distance?.let { put("distance", it.toString()) }
            },
    )
