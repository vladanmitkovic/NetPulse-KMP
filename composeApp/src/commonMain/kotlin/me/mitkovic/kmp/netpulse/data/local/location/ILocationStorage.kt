package me.mitkovic.kmp.netpulse.data.local.location

import kotlinx.coroutines.flow.Flow
import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.domain.model.UserLocation

interface ILocationStorage {
    suspend fun storeCurrentLocation(
        response: GeoIpResponse,
        timestamp: Long,
    )

    fun retrieveCurrentLocation(): Flow<UserLocation?>

    suspend fun clearCurrentLocation()

    suspend fun storeTestLocation(location: UserLocation): Long

    suspend fun getOrStoreTestLocation(location: UserLocation): Long
}
