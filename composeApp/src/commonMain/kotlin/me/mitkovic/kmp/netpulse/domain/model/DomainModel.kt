package me.mitkovic.kmp.netpulse.domain.model

data class ServersResponse(
    val servers: List<Server>,
)

data class Server(
    val url: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val country: String,
    val cc: String,
    val sponsor: String,
    val id: Int,
    val host: String,
    val distance: Double? = null,
)
