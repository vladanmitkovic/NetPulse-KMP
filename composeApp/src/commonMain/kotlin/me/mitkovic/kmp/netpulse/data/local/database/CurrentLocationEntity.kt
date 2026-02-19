package me.mitkovic.kmp.netpulse.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CurrentLocation")
data class CurrentLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ip: String?,
    val network: String?,
    val version: String?,
    val city: String?,
    val region: String?,
    val region_code: String?,
    val country: String?,
    val country_name: String?,
    val country_code: String?,
    val country_code_iso3: String?,
    val country_capital: String?,
    val country_tld: String?,
    val continent_code: String?,
    val in_eu: Long?,
    val postal: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val utc_offset: String?,
    val country_calling_code: String?,
    val currency: String?,
    val currency_name: String?,
    val languages: String?,
    val country_area: Double?,
    val country_population: Long?,
    val asn: String?,
    val org: String?,
    val timestamp: Long,
)
