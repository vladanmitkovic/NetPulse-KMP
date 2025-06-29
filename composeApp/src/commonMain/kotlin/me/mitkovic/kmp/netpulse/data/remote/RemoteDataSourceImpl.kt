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
import me.mitkovic.kmp.netpulse.data.local.LocalDataSource
import me.mitkovic.kmp.netpulse.data.model.Resource
import me.mitkovic.kmp.netpulse.data.model.SpeedTestServersResponse
import me.mitkovic.kmp.netpulse.domain.model.Server
import me.mitkovic.kmp.netpulse.logging.AppLogger
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class RemoteDataSourceImpl(
    private val client: HttpClient,
    private val logger: AppLogger,
    private val xmlFormat: XML,
) : RemoteDataSource {

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

    override suspend fun getSpeedTestServers(): Flow<Resource<SpeedTestServersResponse>> =
        flow {
            emit(Resource.Loading)
            try {
                // Fetch raw XML as String
                val xmlString: String =
                    client
                        .get("${Constants.BASE_URL}/speedtest-servers-static.php") {
                            parameter("threads", "4")
                        }.body<String>()

                // Parse with xmlutil
                val resp: SpeedTestServersResponse =
                    xmlFormat.decodeFromString(
                        deserializer = SpeedTestServersResponse.serializer(),
                        string = xmlString,
                    )

                logger.logDebug(
                    tag = RemoteDataSourceImpl::class.simpleName,
                    message = "resp: $resp",
                )

                emit(Resource.Success(resp))
            } catch (e: Throwable) {
                logger.logError(
                    tag = RemoteDataSourceImpl::class.simpleName,
                    message = "Error fetching XML: ${e.message}",
                    throwable = e,
                )
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }

    override suspend fun findNearestServer(servers: List<Server>): Server? {
        if (servers.isEmpty()) {
            logger.logDebug(RemoteDataSourceImpl::class.simpleName, "No servers available")
            return null
        }

        var lowestLatency = Double.MAX_VALUE
        var nearestServer: Server? = null

        for (server in servers) {
            try {
                val pingUrl = "http://${server.host}"
                val latency =
                    measureTime {
                        val response: HttpResponse = client.head(pingUrl)
                        if (response.status.value !in 200..299) {
                            throw Exception("Invalid response: ${response.status}")
                        }
                    }.inWholeMilliseconds.toDouble()

                logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Latency: $latency ms for server: ${server.host}")
                if (latency > 0 && latency < lowestLatency) {
                    lowestLatency = latency
                    nearestServer = server
                }
            } catch (e: Exception) {
                logger.logError(RemoteDataSourceImpl::class.simpleName, "Failed to ping server ${server.host}: ${e.message}", e)
            }
        }

        logger.logDebug(
            RemoteDataSourceImpl::class.simpleName,
            "Nearest server: ${nearestServer?.name ?: "None"} with latency $lowestLatency ms",
        )
        return nearestServer
    }

    override suspend fun runSpeedTest(
        server: Server,
        localDataSource: LocalDataSource,
    ) = coroutineScope {
        try {
            // Save session to SpeedTestSessionEntity
            val sessionId =
                localDataSource.speedTestResults.insertSpeedTestSession(
                    serverId = server.id.toString(),
                    serverUrl = server.url,
                    serverName = server.name,
                    serverCountry = server.country,
                    serverSponsor = server.sponsor,
                    serverHost = server.host,
                    testTimestamp = Clock.System.now().toEpochMilliseconds(),
                )
            logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Created session with ID: $sessionId")

            // Run download test (10 seconds)
            logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Starting download test")
            downloadTestMultiThread(server = server, initialImageSize = initialImageSize, timeout = 10.0) { speed ->
                if (speed >= 0) {
                    localDataSource.speedTestResults.insertSpeedTestResult(
                        sessionId = sessionId,
                        testType = 1, // Download
                        speed = speed * 8, // Convert MB/s to Mbps
                        resultTimestamp = Clock.System.now().toEpochMilliseconds(),
                    )
                    logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Download speed: $speed MB/s")
                }
            }

            // Run upload test (5 seconds)
            logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Starting upload test")
            uploadTestMultiThread(server = server, initialPayloadSize = initialPayloadSize, timeout = 5.0) { speed ->
                if (speed >= 0) {
                    localDataSource.speedTestResults.insertSpeedTestResult(
                        sessionId = sessionId,
                        testType = 2, // Upload
                        speed = speed * 8, // Convert MB/s to Mbps
                        resultTimestamp = Clock.System.now().toEpochMilliseconds(),
                    )
                    logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Upload speed: $speed MB/s")
                }
            }
        } catch (e: Exception) {
            logger.logError(
                RemoteDataSourceImpl::class.simpleName,
                "Speed test failed: ${e.message}",
                e,
            )
            throw e
        }
    }

    private suspend fun downloadTest(
        server: Server,
        imageSize: String,
    ): Double {
        val timestamp = Clock.System.now().toEpochMilliseconds() / 1000
        val randomId = Random.nextInt(100000, 999999)
        val downloadUrl = server.url.replace("upload.php", "random${imageSize}x$imageSize.jpg?x=$timestamp&y=$randomId")
        logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Download URL: $downloadUrl")

        var totalBytes = 0L
        val duration =
            measureTime {
                val response = client.get(downloadUrl)
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(8192) // 8KB buffer
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytesRead == -1) break
                    totalBytes += bytesRead
                }
            }.toDouble(DurationUnit.SECONDS)

        val sizeInMB = totalBytes / (1024.0 * 1024.0)
        val speed = if (duration > 0) sizeInMB / duration else -1.0
        logger.logDebug(
            RemoteDataSourceImpl::class.simpleName,
            "Downloaded $sizeInMB MB in $duration seconds, speed: $speed MB/s",
        )
        return speed
    }

    private suspend fun downloadTestMultiThread(
        server: Server,
        initialImageSize: String,
        timeout: Double = 10.0,
        onResult: suspend (Double) -> Unit,
    ) = coroutineScope {
        val initialSpeed = downloadTest(server, initialImageSize)
        if (initialSpeed < 0) return@coroutineScope
        logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Initial download speed: $initialSpeed MB/s")
        onResult(initialSpeed)
        val totalSizeInMB = initialSpeed * timeout
        val sizePerThread = totalSizeInMB / 4
        val selectedSize =
            imageSizes.filter { it.second <= sizePerThread }.maxByOrNull { it.second }?.first
                ?: throw IllegalStateException("No suitable image size found")
        val startTime = Clock.System.now().toEpochMilliseconds()
        while (Clock.System.now().toEpochMilliseconds() - startTime < timeout * 1000) {
            val tasks = List(4) { async(Dispatchers.IO) { downloadTest(server, selectedSize) } }
            tasks.awaitAll().forEach { speed -> if (speed >= 0) onResult(speed) }
        }
    }

    private suspend fun uploadTest(
        server: Server,
        payloadSize: Int,
    ): Double {
        val uploadUrl = server.url
        logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Upload URL: $uploadUrl")
        val payload = ByteArray(payloadSize) { 0 }

        val duration =
            measureTime {
                val response =
                    client.post(uploadUrl) {
                        setBody(ByteArrayContent(payload, ContentType.Application.OctetStream))
                    }
                if (response.status.value !in 200..299) {
                    throw Exception("Upload failed with status: ${response.status}")
                }
            }.toDouble(DurationUnit.SECONDS) // Replace .inSeconds with .toDouble(DurationUnit.SECONDS)

        val sizeInMB = payload.size / (1024.0 * 1024.0)
        val speed = if (duration > 0) sizeInMB / duration else -1.0
        logger.logDebug(
            RemoteDataSourceImpl::class.simpleName,
            "Uploaded $sizeInMB MB in $duration seconds, speed: $speed MB/s",
        )
        return speed
    }

    private suspend fun uploadTestMultiThread(
        server: Server,
        initialPayloadSize: Int,
        timeout: Double = 10.0,
        onResult: suspend (Double) -> Unit,
    ) = coroutineScope {
        val initialSpeed = uploadTest(server, initialPayloadSize)
        if (initialSpeed < 0) return@coroutineScope
        logger.logDebug(RemoteDataSourceImpl::class.simpleName, "Initial upload speed: $initialSpeed MB/s")
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
}
