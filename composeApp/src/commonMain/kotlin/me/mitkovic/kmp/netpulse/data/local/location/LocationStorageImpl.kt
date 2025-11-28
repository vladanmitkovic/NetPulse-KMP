package me.mitkovic.kmp.netpulse.data.local.location

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mitkovic.kmp.netpulse.data.local.database.NetPulseDatabase
import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.domain.model.UserLocation

class LocationStorageImpl(
    private val database: NetPulseDatabase,
) : ILocationStorage {

    override suspend fun storeCurrentLocation(
        response: GeoIpResponse,
        timestamp: Long,
    ) {
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.clearCurrentLocation()
            database.netPulseDatabaseQueries.insertCurrentLocation(
                ip = response.ip,
                network = response.network,
                version = response.version,
                city = response.city,
                region = response.region,
                region_code = response.regionCode,
                country = response.country,
                country_name = response.countryName,
                country_code = response.countryCode,
                country_code_iso3 = response.countryCodeIso3,
                country_capital = response.countryCapital,
                country_tld = response.countryTld,
                continent_code = response.continentCode,
                in_eu = response.inEu?.let { if (it) 1L else 0L },
                postal = response.postal,
                latitude = response.latitude,
                longitude = response.longitude,
                timezone = response.timezone,
                utc_offset = response.utcOffset,
                country_calling_code = response.countryCallingCode,
                currency = response.currency,
                currency_name = response.currencyName,
                languages = response.languages,
                country_area = response.countryArea,
                country_population = response.countryPopulation,
                asn = response.asn,
                org = response.org,
                timestamp = timestamp,
            )
        }
    }

    override fun retrieveCurrentLocation(): Flow<UserLocation?> =
        database.netPulseDatabaseQueries
            .getCurrentLocation()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { entity ->
                entity?.let {
                    UserLocation(
                        ip = it.ip,
                        network = it.network,
                        version = it.version,
                        city = it.city,
                        region = it.region,
                        regionCode = it.region_code,
                        country = it.country,
                        countryName = it.country_name,
                        countryCode = it.country_code,
                        countryCodeIso3 = it.country_code_iso3,
                        countryCapital = it.country_capital,
                        countryTld = it.country_tld,
                        continentCode = it.continent_code,
                        inEu = it.in_eu?.let { it != 0L },
                        postal = it.postal,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        timezone = it.timezone,
                        utcOffset = it.utc_offset,
                        countryCallingCode = it.country_calling_code,
                        currency = it.currency,
                        currencyName = it.currency_name,
                        languages = it.languages,
                        countryArea = it.country_area,
                        countryPopulation = it.country_population,
                        asn = it.asn,
                        org = it.org,
                        timestamp = it.timestamp,
                    )
                }
            }

    override suspend fun clearCurrentLocation() {
        database.netPulseDatabaseQueries.clearCurrentLocation()
    }

    override suspend fun storeTestLocation(location: UserLocation): Long {
        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.insertTestLocation(
                ip = location.ip,
                network = location.network,
                version = location.version,
                city = location.city,
                region = location.region,
                region_code = location.regionCode,
                country = location.country,
                country_name = location.countryName,
                country_code = location.countryCode,
                country_code_iso3 = location.countryCodeIso3,
                country_capital = location.countryCapital,
                country_tld = location.countryTld,
                continent_code = location.continentCode,
                in_eu = location.inEu?.let { if (it) 1L else 0L },
                postal = location.postal,
                latitude = location.latitude,
                longitude = location.longitude,
                timezone = location.timezone,
                utc_offset = location.utcOffset,
                country_calling_code = location.countryCallingCode,
                currency = location.currency,
                currency_name = location.currencyName,
                languages = location.languages,
                country_area = location.countryArea,
                country_population = location.countryPopulation,
                asn = location.asn,
                org = location.org,
                timestamp = location.timestamp,
            )
        }
        return database.netPulseDatabaseQueries.lastInsertRowId().executeAsOne()
    }

    override suspend fun getOrStoreTestLocation(location: UserLocation): Long {
        val existingId =
            database.netPulseDatabaseQueries
                .getTestLocationIdByLatLon(location.latitude, location.longitude)
                .executeAsOneOrNull()
        if (existingId != null) {
            return existingId
        }

        database.netPulseDatabaseQueries.transaction {
            database.netPulseDatabaseQueries.insertTestLocation(
                ip = location.ip,
                network = location.network,
                version = location.version,
                city = location.city,
                region = location.region,
                region_code = location.regionCode,
                country = location.country,
                country_name = location.countryName,
                country_code = location.countryCode,
                country_code_iso3 = location.countryCodeIso3,
                country_capital = location.countryCapital,
                country_tld = location.countryTld,
                continent_code = location.continentCode,
                in_eu = location.inEu?.let { if (it) 1L else 0L },
                postal = location.postal,
                latitude = location.latitude,
                longitude = location.longitude,
                timezone = location.timezone,
                utc_offset = location.utcOffset,
                country_calling_code = location.countryCallingCode,
                currency = location.currency,
                currency_name = location.currencyName,
                languages = location.languages,
                country_area = location.countryArea,
                country_population = location.countryPopulation,
                asn = location.asn,
                org = location.org,
                timestamp = location.timestamp,
            )
        }
        return database.netPulseDatabaseQueries.lastInsertRowId().executeAsOne()
    }
}
