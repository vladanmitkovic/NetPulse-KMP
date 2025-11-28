package me.mitkovic.kmp.netpulse.data.mapper

import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.data.model.Server
import me.mitkovic.kmp.netpulse.data.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.UserLocation
import me.mitkovic.kmp.netpulse.domain.model.Server as DomainServer
import me.mitkovic.kmp.netpulse.domain.model.ServersResponse as DomainServersResponse

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

fun GeoIpResponse.toDomainModel(timestamp: Long): UserLocation =
    UserLocation(
        ip = ip,
        network = network,
        version = version,
        city = city,
        region = region,
        regionCode = regionCode,
        country = country,
        countryName = countryName,
        countryCode = countryCode,
        countryCodeIso3 = countryCodeIso3,
        countryCapital = countryCapital,
        countryTld = countryTld,
        continentCode = continentCode,
        inEu = inEu,
        postal = postal,
        latitude = latitude,
        longitude = longitude,
        timezone = timezone,
        utcOffset = utcOffset,
        countryCallingCode = countryCallingCode,
        currency = currency,
        currencyName = currencyName,
        languages = languages,
        countryArea = countryArea,
        countryPopulation = countryPopulation,
        asn = asn,
        org = org,
        timestamp = timestamp,
    )
