package me.mitkovic.kmp.netpulse.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.headers
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import me.mitkovic.kmp.netpulse.common.Constants
import me.mitkovic.kmp.netpulse.data.model.GeoIpResponse
import me.mitkovic.kmp.netpulse.data.model.PingResult
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.ServersResponse
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger
import me.mitkovic.kmp.netpulse.util.calculateAverage
import me.mitkovic.kmp.netpulse.util.calculateJitter
import me.mitkovic.kmp.netpulse.util.calculatePacketLoss
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class RemoteServiceImpl(
    private val client: HttpClient,
    private val logger: AppLogger,
    private val xmlFormat: XML,
) : RemoteService {

    private val imageSizes =
        listOf(
            "350" to 0.25,
            "500" to 0.5,
            "750" to 1.1,
            "1000" to 2.0,
            "1500" to 4.5,
            "2000" to 7.9,
            "2500" to 12.4,
            "3000" to 17.8,
            "3500" to 24.3,
            "4000" to 31.26,
        )

    private val initialImageSize = "1000"
    private val initialPayloadSize = 128 * 1024 // 128 KB

    override suspend fun fetchSpeedTestServers(): Flow<Resource<ServersResponse>> =
        flow {
            emit(Resource.Loading)
            try {
                val xmlString: String =
                    client
                        .get("${Constants.BASE_URL}/speedtest-servers-static.php") {
                            parameter("threads", "4")
                        }.body<String>()
                val resp: ServersResponse =
                    xmlFormat.decodeFromString(
                        deserializer = ServersResponse.serializer(),
                        string = xmlString,
                    )
                logger.logDebug(
                    tag = RemoteServiceImpl::class.simpleName,
                    message = "resp: $resp",
                )
                emit(Resource.Success(resp))
            } catch (e: Throwable) {
                logger.logError(
                    tag = RemoteServiceImpl::class.simpleName,
                    message = "Error fetching XML: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }

    /**
     * Pings a server multiple times using HEAD requests and returns aggregated latency metrics.
     * @param server The server to ping.
     * @param count The number of ping attempts.
     * @return A PingResult containing average latency (ms), jitter (ms), packet loss (%), and failed request count.
     */
    private suspend fun pingServer(
        server: Server,
        count: Int = 3,
    ): PingResult =
        coroutineScope {
            val pingUrl = "http://${server.host}"
            val latencies = mutableListOf<Double>()
            var failedRequests = 0

            // Perform pings sequentially to ensure accurate jitter calculation
            for (i in 0 until count) {
                try {
                    val latency =
                        measureTime {
                            val response: HttpResponse = client.head(pingUrl)
                            if (response.status.value !in 200..299) {
                                throw Exception("Invalid response: ${response.status}")
                            }
                        }.inWholeMilliseconds.toDouble()
                    if (latency > 0) {
                        latencies.add(latency)
                    } else {
                        failedRequests++
                    }
                } catch (e: Exception) {
                    logger.logError(
                        tag = RemoteServiceImpl::class.simpleName,
                        message = "Ping failed for ${server.host}: ${e.message}",
                        throwable = e,
                    )
                    failedRequests++
                }
            }

            // Calculate average latency
            val averageLatency = calculateAverage(latencies)

            // Calculate jitter
            val jitter = calculateJitter(latencies)

            // Calculate packet loss
            val packetLoss = calculatePacketLoss(totalRequests = count, failedRequests = failedRequests)

            logger.logDebug(
                tag = RemoteServiceImpl::class.simpleName,
                message = "Ping results for ${server.host}: averageLatency=$averageLatency ms, jitter=$jitter ms, packetLoss=$packetLoss%, failedRequests=$failedRequests, latencies=$latencies",
            )

            PingResult(averageLatency, jitter, packetLoss, failedRequests)
        }

    override suspend fun findNearestServer(servers: List<Server>): Server? {
        if (servers.isEmpty()) {
            logger.logDebug(RemoteServiceImpl::class.simpleName, "No servers available")
            return null
        }

        // Store ping results for all servers
        val pingResults = mutableListOf<Pair<Server, PingResult>>()

        for (server in servers) {
            try {
                // Ping the server 3 times
                val pingResult = pingServer(server, count = 3)
                pingResults.add(server to pingResult)

                logger.logDebug(
                    tag = RemoteServiceImpl::class.simpleName,
                    message = "Server: ${server.host}, Average Latency: ${pingResult.averageLatency} ms, Jitter: ${pingResult.jitter} ms, Packet Loss: ${pingResult.packetLoss}%, Failed Requests: ${pingResult.failedRequests}",
                )
            } catch (e: Exception) {
                logger.logError(
                    tag = RemoteServiceImpl::class.simpleName,
                    message = "Failed to evaluate server ${server.host}: ${e.message}",
                    throwable = e,
                )
            }
        }

        // Find server with lowest average latency
        val bestResult =
            pingResults
                .filter { it.second.averageLatency > 0 }
                .minByOrNull { it.second.averageLatency }

        val nearestServer = bestResult?.first
        val lowestLatency = bestResult?.second?.averageLatency ?: Double.MAX_VALUE

        logger.logDebug(
            tag = RemoteServiceImpl::class.simpleName,
            message = "Nearest server: ${nearestServer?.name ?: "None"} with average latency $lowestLatency ms",
        )

        return nearestServer
    }

    override suspend fun measurePingAndJitter(server: Server): PingResult = pingServer(server, count = 5) // Adjust count as needed

    private suspend fun downloadTest(
        server: Server,
        imageSize: String,
    ): Double {
        val timestamp = Clock.System.now().toEpochMilliseconds() / 1000
        val randomId = Random.nextInt(100000, 999999)
        val downloadUrl = server.url.replace("upload.php", "random${imageSize}x$imageSize.jpg?x=$timestamp&y=$randomId")
        logger.logDebug(RemoteServiceImpl::class.simpleName, "Download URL: $downloadUrl")
        var totalBytes = 0L
        val duration =
            try {
                measureTime {
                    val response =
                        client.get(downloadUrl) {
                            headers { append("Connection", "keep-alive") }
                        }
                    logger.logDebug(
                        RemoteServiceImpl::class.simpleName,
                        "Response headers: ${response.headers.entries().joinToString { "${it.key}: ${it.value.joinToString()}" }}",
                    )
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(8192)
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead == -1) break
                        totalBytes += bytesRead
                    }
                }.toDouble(DurationUnit.SECONDS)
            } catch (e: Exception) {
                logger.logError(RemoteServiceImpl::class.simpleName, "Download failed: ${e.message}", e)
                return -1.0
            }
        val sizeInMB = totalBytes / (1024.0 * 1024.0)
        val speed = if (duration > 0) sizeInMB / duration else -1.0
        logger.logDebug(
            RemoteServiceImpl::class.simpleName,
            "Downloaded $sizeInMB MB in $duration seconds, speed: $speed MB/s",
        )
        return speed
    }

    override suspend fun downloadTestMultiThread(
        server: Server,
        initialImageSize: String,
        timeout: Double,
        onResult: suspend (Double) -> Unit,
    ) = coroutineScope {
        val initialSpeed = downloadTest(server, initialImageSize)
        if (initialSpeed < 0) return@coroutineScope
        logger.logDebug(RemoteServiceImpl::class.simpleName, "Initial download speed: $initialSpeed MB/s")
        onResult(initialSpeed)
        val totalSizeInMB = initialSpeed * timeout
        val sizePerThread = totalSizeInMB / 4
        val selectedSize =
            imageSizes
                .filter { it.second <= sizePerThread }
                .maxByOrNull { it.second }
                ?.first
                ?: throw Exception("No suitable image size found for speed $initialSpeed MB/s")
        val startTime = Clock.System.now().toEpochMilliseconds()
        while (Clock.System.now().toEpochMilliseconds() - startTime < timeout * 1000) {
            val tasks = List(2) { async(Dispatchers.IO) { downloadTest(server, selectedSize) } }
            tasks.awaitAll().forEach { speed -> if (speed >= 0) onResult(speed) }
        }
    }

    private suspend fun uploadTest(
        server: Server,
        payloadSize: Int,
    ): Double {
        val uploadUrl = server.url
        logger.logDebug(RemoteServiceImpl::class.simpleName, "Upload URL: $uploadUrl")
        val payload = ByteArray(payloadSize) { 0 }
        val duration =
            measureTime {
                val response =
                    client.post(uploadUrl) {
                        headers { append("Connection", "keep-alive") }
                        setBody(ByteArrayContent(payload, ContentType.Application.OctetStream))
                    }
                logger.logDebug(
                    RemoteServiceImpl::class.simpleName,
                    "Response headers: ${response.headers.entries().joinToString { "${it.key}: ${it.value.joinToString()}" }}",
                )
                if (response.status.value !in 200..299) {
                    throw Exception("Upload failed with status: ${response.status}")
                }
            }.toDouble(DurationUnit.SECONDS)
        val sizeInMB = payload.size / (1024.0 * 1024.0)
        val speed = if (duration > 0) sizeInMB / duration else -1.0
        logger.logDebug(
            RemoteServiceImpl::class.simpleName,
            "Uploaded $sizeInMB MB in $duration seconds, speed: $speed MB/s",
        )
        return speed
    }

    override suspend fun uploadTestMultiThread(
        server: Server,
        initialPayloadSize: Int,
        timeout: Double,
        onResult: suspend (Double) -> Unit,
    ) = coroutineScope {
        val initialSpeed = uploadTest(server, initialPayloadSize)
        if (initialSpeed < 0) return@coroutineScope
        logger.logDebug(RemoteServiceImpl::class.simpleName, "Initial upload speed: $initialSpeed MB/s")
        onResult(initialSpeed)
        val totalSizeInMB = initialSpeed * timeout
        val totalSizeInBytes = (totalSizeInMB * 1024 * 1024).toInt()
        val sizePerThread = totalSizeInBytes / 2
        val startTime = Clock.System.now().toEpochMilliseconds()
        while (Clock.System.now().toEpochMilliseconds() - startTime < timeout * 1000) {
            val tasks = List(2) { async(Dispatchers.IO) { uploadTest(server, sizePerThread) } }
            tasks.awaitAll().forEach { speed -> if (speed >= 0) onResult(speed) }
        }
    }

    override suspend fun getUserLocation(): GeoIpResponse? =
        try {
            val response: GeoIpResponse = client.get("https://ipapi.co/json/").body()
            if (response.error == true) {
                logger.logError(RemoteServiceImpl::class.simpleName, "GeoIP error: ${response.reason}", null)
                null
            } else {
                response
            }
        } catch (e: Exception) {
            logger.logError(RemoteServiceImpl::class.simpleName, "Failed to get user location: ${e.message}", e)
            null
        }
}
