package me.mitkovic.kmp.netpulse.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoIpErrorResponse(
    val error: Boolean,
    val reason: String? = null,
    val message: String? = null,
)
