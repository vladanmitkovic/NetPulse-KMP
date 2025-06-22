package me.mitkovic.kmp.netpulse.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse
import me.mitkovic.kmp.netpulse.logging.AppLogger
import nl.adaptivity.xmlutil.serialization.XML

class RemoteDataSourceImpl(
    private val client: HttpClient,
    private val logger: AppLogger,
    private val xmlFormat: XML,
) : RemoteDataSource {

    override suspend fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse>> =
        flow {
            emit(Resource.Loading)
            try {
                // 1) fetch raw XML as String
                val xmlString: String =
                    client
                        .get("${Constants.BASE_URL}/speedtest-servers-static.php") {
                            parameter("threads", "4")
                        }.body<String>()

                // 2) parse with xmlutil
                val resp: SpeedTestServersResponse =
                    xmlFormat.decodeFromString(
                        deserializer = SpeedTestServersResponse.serializer(),
                        string = xmlString,
                    )

                emit(Resource.Success(resp))
            } catch (e: Throwable) {
                logger.logError(
                    tag = "RemoteDataSource",
                    message = "Error fetching XML: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
}
