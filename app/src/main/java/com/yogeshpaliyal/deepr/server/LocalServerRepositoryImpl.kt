package com.yogeshpaliyal.deepr.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.util.Locale

class LocalServerRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val accountViewModel: AccountViewModel,
    private val networkRepository: NetworkRepository,
) : LocalServerRepository {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)
    override val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val port = 8080

    override suspend fun startServer() {
        if (_isRunning.value) {
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
                                            tags = link.tagsNames?.split(", ")?.filter { it.isNotEmpty() } ?: emptyList(),
                                        )
                                    }
                                call.respond(HttpStatusCode.OK, response)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting links", e)
                                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error getting links: ${e.message}"))
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
                                call.respond(HttpStatusCode.Created, SuccessResponse("Link added successfully"))
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error adding link", e)
                                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error adding link: ${e.message}"))
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
                                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error getting tags: ${e.message}"))
                            }
                        }

                        get("/api/link-info") {
                            try {
                                val url = call.request.queryParameters["url"]
                                if (url.isNullOrBlank()) {
                                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("URL parameter is required"))
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
                                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error getting link info: ${e.message}"))
                            }
                        }

                        get("/api/server-info") {
                            try {
                                val ipAddress = getIpAddress()
                                val serverInfo =
                                    ServerInfoResponse(
                                        ipAddress = ipAddress ?: "Unknown",
                                        port = port,
                                        baseUrl = if (ipAddress != null) "http://$ipAddress:$port" else null,
                                        endpoints =
                                            listOf(
                                                ApiEndpoint(
                                                    method = "GET",
                                                    path = "/api/links",
                                                    description = "Retrieve all saved links with their metadata",
                                                ),
                                                ApiEndpoint(
                                                    method = "POST",
                                                    path = "/api/links",
                                                    description = "Add a new link to your collection",
                                                ),
                                                ApiEndpoint(
                                                    method = "GET",
                                                    path = "/api/tags",
                                                    description = "Get all available tags with usage count",
                                                ),
                                                ApiEndpoint(
                                                    method = "GET",
                                                    path = "/api/link-info",
                                                    description = "Fetch metadata (title, image) for any URL",
                                                ),
                                                ApiEndpoint(
                                                    method = "GET",
                                                    path = "/api/server-info",
                                                    description = "Get server information and available API endpoints",
                                                ),
                                            ),
                                    )
                                call.respond(HttpStatusCode.OK, serverInfo)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting server info", e)
                                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error getting server info: ${e.message}"))
                            }
                        }
                    }
                }

            server?.start(wait = false)
            _isRunning.value = true
            _serverUrl.value = "http://$ipAddress:$port"
            Log.d("LocalServer", "Server started at ${_serverUrl.value}")
        } catch (e: Exception) {
            Log.e("LocalServer", "Error starting server", e)
            _isRunning.value = false
            _serverUrl.value = null
        }
    }

    override suspend fun stopServer() {
        try {
            server?.stop(1000, 2000)
            server = null
            _isRunning.value = false
            _serverUrl.value = null
            Log.d("LocalServer", "Server stopped")
        } catch (e: Exception) {
            Log.e("LocalServer", "Error stopping server", e)
        }
    }

    private fun getIpAddress(): String? {
        try {
            // Try to get WiFi IP first
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
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
    fun toDbTag() = com.yogeshpaliyal.deepr.Tags(id, name)
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
data class ServerInfoResponse(
    val ipAddress: String,
    val port: Int,
    val baseUrl: String?,
    val endpoints: List<ApiEndpoint>,
)

@Serializable
data class ApiEndpoint(
    val method: String,
    val path: String,
    val description: String,
)
