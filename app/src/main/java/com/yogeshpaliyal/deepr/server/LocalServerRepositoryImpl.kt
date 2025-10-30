package com.yogeshpaliyal.deepr.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
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
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.util.Locale

class LocalServerRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val httpClient: HttpClient,
    private val accountViewModel: AccountViewModel,
    private val networkRepository: NetworkRepository,
    private val analyticsManager: AnalyticsManager,
    private val preferenceDataStore: AppPreferenceDataStore,
) : LocalServerRepository {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? =
        null
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)
    override val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)
    override val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _qrCodeData = MutableStateFlow<String?>(null)
    override val qrCodeData: StateFlow<String?> = _qrCodeData

    private val _isTransferLinkServerRunning = MutableStateFlow(false)
    override val isTransferLinkServerRunning: StateFlow<Boolean> =
        _isTransferLinkServerRunning.asStateFlow()

    private val _transferLinkServerUrl = MutableStateFlow<String?>(null)
    override val transferLinkServerUrl: StateFlow<String?> = _transferLinkServerUrl.asStateFlow()

    init {
        // Load saved port on initialization
        CoroutineScope(Dispatchers.IO).launch {
            preferenceDataStore.getServerPort.collect { portString ->
                val port = portString.toIntOrNull()
                if (port != null && port in 1024..65535) {
                    _serverPort.value = port
                } else {
                    _serverPort.value = 8080
                }
            }
        }
    }

    override suspend fun setServerPort(port: Int) {
        if (port in 1024..65535) {
            _serverPort.value = port
            preferenceDataStore.setServerPort(port.toString())
        }
    }

    override suspend fun startServer(port: Int) {
        if (isRunning.value || isTransferLinkServerRunning.value) {
            if (port == 9000) {
                generateQRCode(port)?.let { qrData -> _qrCodeData.update { qrData } }
            }
            Log.d("LocalServer", "Server is already running")
            return
        }

        try {
            val ipAddress = getIpAddress()
            if (ipAddress == null) {
                Log.e("LocalServer", "Unable to get IP address")
                return
            }

            val port = _serverPort.value

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
                        get("/") {
                            try {
                                val htmlContent =
                                    context.assets
                                        .open("index.html")
                                        .bufferedReader()
                                        .use { it.readText() }
                                call.respondText(htmlContent, ContentType.Text.Html)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error reading HTML asset", e)
                                call.respondText(
                                    """
                                    <html>
                                    <body>
                                        <h1>Deepr Server</h1>
                                        <p>Error loading interface. Please check server logs.</p>
                                    </body>
                                    </html>
                                    """.trimIndent(),
                                    ContentType.Text.Html,
                                )
                            }
                        }

                        get("/api/links") {
                            try {
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
                                val response =
                                    links.map { link ->
                                        LinkResponse(
                                            id = link.id,
                                            link = link.link,
                                            name = link.name,
                                            createdAt = link.createdAt,
                                            openedCount = link.openedCount,
                                            notes = link.notes,
                                            tags =
                                                link.tagsNames
                                                    ?.split(", ")
                                                    ?.filter { it.isNotEmpty() } ?: emptyList(),
                                        )
                                    }
                                call.respond(HttpStatusCode.OK, response)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting links", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error getting links: ${e.message}"),
                                )
                            }
                        }

                        post("/api/links") {
                            try {
                                val request = call.receive<AddLinkRequest>()
                                // Insert the link without tags first
                                accountViewModel.insertAccount(
                                    request.link,
                                    request.name,
                                    false,
                                    request.tags.map { it.toDbTag() },
                                    request.notes,
                                )
                                call.respond(
                                    HttpStatusCode.Created,
                                    SuccessResponse("Link added successfully"),
                                )
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error adding link", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error adding link: ${e.message}"),
                                )
                            }
                        }

                        get("/api/tags") {
                            try {
                                // Get all tags from the database with their IDs
                                val allTags = deeprQueries.getAllTags().executeAsList()
                                val response =
                                    allTags.map { tag ->
                                        // Count how many links use this tag
                                        val linkCount =
                                            deeprQueries
                                                .getLinksAndTags(
                                                    "",
                                                    "",
                                                    "",
                                                    -1L,
                                                    -1L,
                                                    tag.id.toString(),
                                                    tag.id.toString(),
                                                    1L,
                                                    "DESC",
                                                    "createdAt",
                                                    "DESC",
                                                    "createdAt",
                                                ).executeAsList()
                                                .size
                                        TagResponse(
                                            id = tag.id,
                                            name = tag.name,
                                            count = linkCount,
                                        )
                                    }
                                call.respond(HttpStatusCode.OK, response)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting tags", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error getting tags: ${e.message}"),
                                )
                            }
                        }

                        get("/api/link-info") {
                            try {
                                val url = call.request.queryParameters["url"]
                                if (url.isNullOrBlank()) {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse("URL parameter is required"),
                                    )
                                    return@get
                                }

                                val result = networkRepository.getLinkInfo(url)
                                if (result.isSuccess) {
                                    val linkInfo = result.getOrNull()
                                    call.respond(
                                        HttpStatusCode.OK,
                                        LinkInfoResponse(
                                            title = linkInfo?.title,
                                            imageUrl = linkInfo?.image,
                                        ),
                                    )
                                } else {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse("Error fetching link info: ${result.exceptionOrNull()?.message}"),
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting link info", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error getting link info: ${e.message}"),
                                )
                            }
                        }
                    }
                }

            server?.start(wait = false)
            if (port == 9000) {
                val generatedQrData = generateQRCode(port)
                _qrCodeData.update { generatedQrData }
                _isTransferLinkServerRunning.update { true }
                _transferLinkServerUrl.update { "http://$ipAddress:$port" }
                Log.d("LocalServer", "Server started at ${_transferLinkServerUrl.value}")
                analyticsManager.logEvent(
                    com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.START_LOCAL_SERVER,
                    mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.SERVER_PORT to port),
                )
            } else {
                _isRunning.update { true }
                _serverUrl.update { "http://$ipAddress:$port" }
                Log.d("LocalServer", "Server started at ${_serverUrl.value}")
            }
        } catch (e: Exception) {
            Log.e("LocalServer", "Error starting server", e)
            if (port == 9000) {
                _isTransferLinkServerRunning.update { false }
                _transferLinkServerUrl.update { null }
            } else {
                _isRunning.update { false }
                _serverUrl.update { null }
            }
        }
    }

    override suspend fun stopServer() {
        try {
            server?.stop(1000, 2000)
            server = null
            _isRunning.update { false }
            _serverUrl.update { null }
            _isTransferLinkServerRunning.update { false }
            _transferLinkServerUrl.update { null }
            Log.d("LocalServer", "Server stopped")
            analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.STOP_LOCAL_SERVER)
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

                Log.d("Anas", response.toString())

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

    private fun generateQRCode(port: Int): String? {
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
data class LinkResponse(
    val id: Long,
    val link: String,
    val name: String,
    val createdAt: String,
    val openedCount: Long,
    val notes: String,
    val tags: List<String>,
)

@Serializable
data class TagData(
    val id: Long,
    val name: String,
) {
    fun toDbTag() = Tags(id, name)
}

@Serializable
data class AddLinkRequest(
    val link: String,
    val name: String,
    val notes: String = "",
    val tags: List<TagData> = emptyList(),
)

@Serializable
data class LinkInfoResponse(
    val title: String?,
    val imageUrl: String?,
)

@Serializable
data class SuccessResponse(
    val message: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
)

@Serializable
data class TagResponse(
    val id: Long,
    val name: String,
    val count: Int,
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
