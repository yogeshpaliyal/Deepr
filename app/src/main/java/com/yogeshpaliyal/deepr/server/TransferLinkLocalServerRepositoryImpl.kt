package com.yogeshpaliyal.deepr.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.DeeprQueries
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.util.Locale

class TransferLinkLocalServerRepositoryImpl(
    private val context: Context,
    private val httpClient: HttpClient,
    private val deeprQueries: DeeprQueries,
) : TransferLinkLocalServerRepository {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? =
        null
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)
    override val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _qrCodeData = MutableStateFlow<String?>(null)
    override val qrCodeData: StateFlow<String?> = _qrCodeData.asStateFlow()

    private val port = 9000

    override suspend fun startServer() {
        if (isRunning.value) {
            generateQRCode()?.let { qrData -> _qrCodeData.update { qrData } }
            Log.d("LocalServer", "Server is already running")
            return
        }

        try {
            val ipAddress = getIpAddress()
            if (ipAddress == null) {
                Log.e("LocalServer", "Unable to get IP address")
                return
            }

            server =
                embeddedServer(CIO, host = "0.0.0.0", port = port) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            },
                        )
                    }

                    routing {
                        get("/api/export") {
                            try {
                                // Get all links
                                val links =
                                    deeprQueries
                                        .getLinksAndTags(
                                            "",
                                            "",
                                            "",
                                            -1L,
                                            -1L,
                                            "",
                                            "",
                                            0L,
                                            "DESC",
                                            "createdAt",
                                            "DESC",
                                            "createdAt",
                                        ).executeAsList()

                                // Get all tags
                                val tags = deeprQueries.getAllTags().executeAsList()

                                // Create export response
                                val exportData =
                                    ExportDataResponse(
                                        appVersion = BuildConfig.VERSION_NAME,
                                        exportedAt = System.currentTimeMillis(),
                                        links =
                                            links.map { link ->
                                                ExportedDeeplink(
                                                    link = link.link,
                                                    name = link.name,
                                                    createdAt = link.createdAt,
                                                    openedCount = link.openedCount,
                                                    notes = link.notes,
                                                    tags =
                                                        link.tagsNames
                                                            ?.split(", ")
                                                            ?.filter { it.isNotEmpty() }
                                                            ?: emptyList(),
                                                    isFavourite = link.isFavourite == 1L,
                                                    thumbnail = link.thumbnail,
                                                )
                                            },
                                        tags =
                                            tags.map { tag ->
                                                TagData(id = tag.id, name = tag.name)
                                            },
                                    )

                                call.respond(HttpStatusCode.OK, exportData)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error exporting data", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error exporting data: ${e.message}"),
                                )
                            }
                        }
                    }
                }

            server?.start(wait = false)
            val generatedQrData = generateQRCode()
            _qrCodeData.update { generatedQrData }
            _isRunning.update { true }
            _serverUrl.update { "http://$ipAddress:$port" }
            Log.d("LocalServer", "Server started at ${_serverUrl.value}")
        } catch (e: Exception) {
            Log.e("LocalServer", "Error starting server", e)
            _isRunning.update { false }
            _serverUrl.update { null }
        }
    }

    override suspend fun stopServer() {
        try {
            server?.stop(1000, 2000)
            server = null
            _isRunning.update { false }
            _serverUrl.update { null }
            Log.d("LocalServer", "Server stopped")
        } catch (e: Exception) {
            Log.e("LocalServer", "Error stopping server", e)
        }
    }

    override suspend fun fetchAndImportFromSender(qrTransferInfo: QRTransferInfo): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse =
                    httpClient.get {
                        url {
                            protocol = URLProtocol.HTTP
                            host = qrTransferInfo.ip
                            port = qrTransferInfo.port
                            path("api/export")
                        }
                        timeout {
                            requestTimeoutMillis = 30000 // 30 seconds
                        }
                    }

                if (response.status.isSuccess().not()) {
                    return@withContext Result.failure(
                        Exception("Failed to fetch data: ${response.status}"),
                    )
                }

                val exportedData: ExportedData = response.body()

                importToDatabase(exportedData)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun importToDatabase(data: ExportedData) {
        deeprQueries.transaction {
            data.links.forEach { deeplink ->
                if (deeprQueries.getDeeprByLink(deeplink.link).executeAsList().isEmpty()) {
                    deeprQueries.insertDeepr(
                        link = deeplink.link,
                        name = deeplink.name,
                        openedCount = deeplink.openedCount,
                        notes = deeplink.notes,
                        thumbnail = deeplink.thumbnail,
                    )

                    val insertedId = deeprQueries.lastInsertRowId().executeAsOne()

                    deeplink.tags.forEach { tagName ->
                        deeprQueries.insertTag(name = tagName)

                        val tag = deeprQueries.getTagByName(tagName).executeAsOne()

                        deeprQueries.addTagToLink(
                            linkId = insertedId,
                            tagId = tag.id,
                        )
                    }

                    if (deeplink.isFavourite) {
                        deeprQueries.setFavourite(
                            isFavourite = 1,
                            id = insertedId,
                        )
                    }
                }
            }

            data.tags.forEach { tagName ->
                deeprQueries.insertTag(name = tagName)
            }
        }
    }

    private fun generateQRCode(): String? {
        val ipAddress = getIpAddress() ?: return null

        val qrInfo =
            QRTransferInfo(
                ip = ipAddress,
                port = port,
                appVersion = BuildConfig.VERSION_NAME,
            )

        return try {
            Json.encodeToString(QRTransferInfo.serializer(), qrInfo)
        } catch (e: Exception) {
            Log.e("LocalServer", "Error generating QR code data", e)
            null
        }
    }

    private fun getIpAddress(): String? {
        try {
            // Try to get WiFi IP first
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.connectionInfo?.ipAddress?.let { ipInt ->
                if (ipInt != 0) {
                    return String.format(
                        Locale.US,
                        "%d.%d.%d.%d",
                        ipInt and 0xff,
                        ipInt shr 8 and 0xff,
                        ipInt shr 16 and 0xff,
                        ipInt shr 24 and 0xff,
                    )
                }
            }

            // Fallback to network interfaces
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.contains(':') == false) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocalServer", "Error getting IP address", e)
        }
        return null
    }
}

@Serializable
data class ExportDataResponse(
    val appVersion: String,
    val exportedAt: Long,
    val links: List<ExportedDeeplink>,
    val tags: List<TagData>,
)

@Serializable
data class QRTransferInfo(
    val ip: String,
    val port: Int,
    val appVersion: String,
)

@Serializable
data class ExportedData(
    val links: List<ExportedDeeplink>,
    val tags: List<String>,
    val exportedAt: Long,
)

@Serializable
data class ExportedDeeplink(
    val link: String,
    val name: String,
    val notes: String,
    val tags: List<String>,
    val openedCount: Long,
    val isFavourite: Boolean,
    val createdAt: String,
    val thumbnail: String,
)
