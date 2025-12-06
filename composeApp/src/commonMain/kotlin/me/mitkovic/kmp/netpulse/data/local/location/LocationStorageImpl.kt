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
                entity?.let { currentLocation ->
                    UserLocation(
                        ip = currentLocation.ip,
                        network = currentLocation.network,
                        version = currentLocation.version,
                        city = currentLocation.city,
                        region = currentLocation.region,
                        regionCode = currentLocation.region_code,
                        country = currentLocation.country,
                        countryName = currentLocation.country_name,
                        countryCode = currentLocation.country_code,
                        countryCodeIso3 = currentLocation.country_code_iso3,
                        countryCapital = currentLocation.country_capital,
                        countryTld = currentLocation.country_tld,
                        continentCode = currentLocation.continent_code,
                        inEu = currentLocation.in_eu?.let { it != 0L },
                        postal = currentLocation.postal,
                        latitude = currentLocation.latitude,
                        longitude = currentLocation.longitude,
                        timezone = currentLocation.timezone,
                        utcOffset = currentLocation.utc_offset,
                        countryCallingCode = currentLocation.country_calling_code,
                        currency = currentLocation.currency,
                        currencyName = currentLocation.currency_name,
                        languages = currentLocation.languages,
                        countryArea = currentLocation.country_area,
                        countryPopulation = currentLocation.country_population,
                        asn = currentLocation.asn,
                        org = currentLocation.org,
                        timestamp = currentLocation.timestamp,
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
