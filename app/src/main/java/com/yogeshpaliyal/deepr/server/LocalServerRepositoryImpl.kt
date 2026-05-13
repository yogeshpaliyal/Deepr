package com.yogeshpaliyal.deepr.server

import android.content.Context
import android.content.res.Configuration
import android.net.wifi.WifiManager
import android.util.Log
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.Tags
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.LanguageUtil
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.util.Locale

/**
 * Implementation of [LocalServerRepository] that runs an embedded Ktor server
 * to provide a web interface and API for managing links and profiles.
 */
open class LocalServerRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val httpClient: HttpClient,
    private val accountViewModel: AccountViewModel,
    private val networkRepository: NetworkRepository,
    private val analyticsManager: AnalyticsManager,
    private val preferenceDataStore: AppPreferenceDataStore,
) : LocalServerRepository {
    companion object {
        /**
         * Cache for generated HTML content indexed by [Locale].
         */
        private val htmlCache = mutableMapOf<Locale, String>()
    }

    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? =
        null

    private val _isRunning = MutableStateFlow(false)

    /**
     * [StateFlow] indicating whether the server is currently running.
     */
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)

    /**
     * [StateFlow] providing the URL where the server is accessible.
     */
    override val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _serverPort = MutableStateFlow(8080)

    /**
     * [StateFlow] providing the current server port.
     */
    override val serverPort: StateFlow<Int> = _serverPort.asStateFlow()

    private val _qrCodeData = MutableStateFlow<String?>(null)

    /**
     * [StateFlow] providing the JSON data for generating a QR code to connect to the server.
     */
    override val qrCodeData: StateFlow<String?> = _qrCodeData

    init {
        // Load saved port on initialization
        CoroutineScope(Dispatchers.IO).launch {
            preferenceDataStore.getServerPort.collect { portString ->
                val port = portString.toIntOrNull()
                if (port != null && port in 1024..65535) {
                    _serverPort.update { port }
                } else {
                    _serverPort.update { 8080 }
                }
            }
        }
    }

    /**
     * Sets the server port and persists it to preferences.
     * @param port The port number (must be between 1024 and 65535).
     */
    override suspend fun setServerPort(port: Int) {
        if (port in 1024..65535) {
            _serverPort.update { port }
            preferenceDataStore.setServerPort(port.toString())
        }
    }

    /**
     * Starts the embedded Ktor server on the specified port.
     * @param port The port to listen on.
     */
    override suspend fun startServer(port: Int) {
        if (isRunning.value) {
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
                                val languageCode = preferenceDataStore.getLanguageCode.first()
                                val localizedContext = LanguageUtil.updateLocale(context, languageCode)
                                val locale =
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                        localizedContext.resources.configuration.locales[0]
                                    } else {
                                        @Suppress("DEPRECATION")
                                        localizedContext.resources.configuration.locale
                                    }

                                val cachedHtmlContent = htmlCache[locale]
                                if (cachedHtmlContent != null) {
                                    call.respondText(cachedHtmlContent, ContentType.Text.Html)
                                    return@get
                                }

                                var htmlContent =
                                    context.assets
                                        .open("index.html")
                                        .bufferedReader()
                                        .use { it.readText() }

                                if (htmlContent.contains("{{WEB_")) {
                                    // Replace placeholders with translations
                                    val placeholders =
                                        mapOf(
                                            "{{WEB_TITLE}}" to R.string.web_title,
                                            "{{WEB_SUBTITLE}}" to R.string.web_subtitle,
                                            "{{WEB_PROFILE_LABEL}}" to R.string.web_profile_label,
                                            "{{WEB_NEW_PROFILE}}" to R.string.web_new_profile,
                                            "{{WEB_ADD_NEW_LINK}}" to R.string.web_add_new_link,
                                            "{{WEB_LINK_URL}}" to R.string.web_link_url,
                                            "{{WEB_LINK_NAME}}" to R.string.web_link_name,
                                            "{{WEB_OPTIONAL}}" to R.string.web_optional,
                                            "{{WEB_NOTES_OPTIONAL}}" to R.string.web_notes_optional,
                                            "{{WEB_TAGS_OPTIONAL}}" to R.string.web_tags_optional,
                                            "{{WEB_ADD_LINK_BUTTON}}" to R.string.web_add_link_button,
                                            "{{WEB_YOUR_LINKS}}" to R.string.web_your_links,
                                            "{{WEB_REFRESH}}" to R.string.web_refresh,
                                            "{{WEB_SEARCH_PLACEHOLDER}}" to R.string.web_search_placeholder,
                                            "{{WEB_ALL_TAGS}}" to R.string.web_all_tags,
                                            "{{WEB_SORT_DATE}}" to R.string.web_sort_date,
                                            "{{WEB_SORT_NAME}}" to R.string.web_sort_name,
                                            "{{WEB_SORT_OPENS}}" to R.string.web_sort_opens,
                                            "{{WEB_CLEAR}}" to R.string.web_clear,
                                            "{{WEB_LOADING_LINKS}}" to R.string.web_loading_links,
                                            "{{WEB_LOADING_DESCRIPTION}}" to R.string.web_loading_description,
                                            "{{WEB_CREATE_PROFILE_TITLE}}" to R.string.web_create_profile_title,
                                            "{{WEB_PROFILE_NAME_LABEL}}" to R.string.web_profile_name_label,
                                            "{{WEB_CANCEL}}" to R.string.web_cancel,
                                            "{{WEB_CREATE_PROFILE_BUTTON}}" to R.string.web_create_profile_button,
                                            "{{WEB_STATUS_CREATING}}" to R.string.web_status_creating,
                                            "{{WEB_STATUS_ADDED}}" to R.string.web_status_added,
                                            "{{WEB_SUCCESS_LINK_ADDED}}" to R.string.web_success_link_added,
                                            "{{WEB_ERROR_ADD_LINK}}" to R.string.web_error_add_link,
                                            "{{WEB_ERROR_LOADING}}" to R.string.web_error_loading,
                                            "{{WEB_ERROR_REFRESH}}" to R.string.web_error_refresh,
                                            "{{WEB_ERROR_FETCH}}" to R.string.web_error_fetch,
                                            "{{WEB_MSG_SWITCHED}}" to R.string.web_msg_switched,
                                            "{{WEB_MSG_FILTERED}}" to R.string.web_msg_filtered,
                                            "{{WEB_MSG_FILTERS_CLEARED}}" to R.string.web_msg_filters_cleared,
                                            "{{WEB_NO_LINKS}}" to R.string.web_no_links,
                                            "{{WEB_NO_LINKS_DESCRIPTION}}" to R.string.web_no_links_description,
                                            "{{WEB_NO_MATCHING}}" to R.string.web_no_matching,
                                            "{{WEB_NO_MATCHING_DESCRIPTION}}" to R.string.web_no_matching_description,
                                            "{{WEB_OPEN_LINK}}" to R.string.web_open_link,
                                            "{{WEB_OPENS}}" to R.string.web_opens,
                                            "{{WEB_LINK_URL_PLACEHOLDER}}" to R.string.link_placeholder,
                                            "{{WEB_LINK_NAME_PLACEHOLDER}}" to R.string.enter_link_name,
                                            "{{WEB_NOTES_PLACEHOLDER}}" to R.string.enter_notes,
                                            "{{WEB_TAGS_PLACEHOLDER}}" to R.string.web_tags_optional,
                                            "{{WEB_PROFILE_PLACEHOLDER}}" to R.string.enter_profile_name,
                                            "{{WEB_UNKNOWN}}" to R.string.unknown,
                                        )

                                    placeholders.forEach { (placeholder, resId) ->
                                        htmlContent = htmlContent.replace(placeholder, localizedContext.getString(resId))
                                    }
                                }

                                htmlCache[locale] = htmlContent
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

                        get("/api/profiles") {
                            try {
                                val profiles = deeprQueries.getAllProfiles().executeAsList()
                                val response =
                                    profiles.map { profile ->
                                        ProfileResponse(
                                            id = profile.id,
                                            name = profile.name,
                                            createdAt = profile.createdAt,
                                        )
                                    }
                                call.respond(HttpStatusCode.OK, response)
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error getting profiles", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error getting profiles: ${e.message}"),
                                )
                            }
                        }

                        post("/api/profiles") {
                            try {
                                val request = call.receive<AddProfileRequest>()
                                deeprQueries.insertProfile(request.name)
                                call.respond(
                                    HttpStatusCode.Created,
                                    SuccessResponse("Profile created successfully"),
                                )
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error creating profile", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error creating profile: ${e.message}"),
                                )
                            }
                        }

                        get("/api/links") {
                            try {
                                val profileIdParam = call.request.queryParameters["profileId"]
                                val profileId =
                                    if (profileIdParam == null) {
                                        1L
                                    } else {
                                        profileIdParam.toLongOrNull() ?: run {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                ErrorResponse("Invalid profileId: $profileIdParam"),
                                            )
                                            return@get
                                        }
                                    }
                                val links =
                                    deeprQueries
                                        .getLinksAndTags(
                                            profileId = profileId,
                                            query = "",
                                            isFavourite = -1L,
                                            tagId = "",
                                            sortOrder = "DESC",
                                            orderBy = "createdAt",
                                        ).executeAsList()
                                val response =
                                    links.map { link ->
                                        LinkResponse(
                                            id = link.id,
                                            link = link.link,
                                            name = link.name,
                                            createdAt = link.createdAt,
                                            isFavourite = link.isFavourite,
                                            openedCount = link.openedCount,
                                            notes = link.notes,
                                            thumbnail = link.thumbnail,
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
                                    link = request.link,
                                    name = request.name,
                                    executed = false,
                                    tagsList = request.tags.map { it.toDbTag() },
                                    notes = request.notes,
                                    thumbnail = "",
                                    profileId = request.profileId,
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
                                val profileIdParam = call.request.queryParameters["profileId"]
                                val profileId =
                                    if (profileIdParam == null) {
                                        1L
                                    } else {
                                        profileIdParam.toLongOrNull() ?: run {
                                            call.respond(
                                                HttpStatusCode.BadRequest,
                                                ErrorResponse("Invalid profileId: $profileIdParam"),
                                            )
                                            return@get
                                        }
                                    }
                                val allTags = deeprQueries.getAllTagsWithCount(profileId = profileId).executeAsList()
                                val response =
                                    allTags.map { tag ->
                                        TagResponse(
                                            id = tag.id,
                                            name = tag.name,
                                            count = tag.linkCount.toInt(),
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

                        post("/api/links/increment-count") {
                            try {
                                val id = call.request.queryParameters["id"]?.toLongOrNull()
                                if (id != null) {
                                    accountViewModel.incrementOpenedCount(id)
                                    call.respond(HttpStatusCode.OK, SuccessResponse("Count incremented"))
                                } else {
                                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid link ID"))
                                }
                            } catch (e: Exception) {
                                Log.e("LocalServer", "Error incrementing count", e)
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                    ErrorResponse("Error incrementing count: ${e.message}"),
                                )
                            }
                        }
                    }
                }

            server?.start(wait = false)

            _isRunning.update { true }
            _serverUrl.update { "http://$ipAddress:$port" }
            Log.d("LocalServer", "Server started at ${serverUrl.value}")

            if (port == 9000) {
                generateQRCode(port)?.let { qrData -> _qrCodeData.update { qrData } }
            }

            analyticsManager.logEvent(
                com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.START_LOCAL_SERVER,
                mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.SERVER_PORT to port),
            )
        } catch (e: Exception) {
            Log.e("LocalServer", "Error starting server", e)

            _isRunning.update { false }
            _serverUrl.update { null }
        }
    }

    /**
     * Stops the embedded Ktor server.
     */
    override fun stopServer() {
        try {
            server?.stop(1000, 2000)
            server = null
            _isRunning.update { false }
            _serverUrl.update { null }
            Log.d("LocalServer", "Server stopped")
            analyticsManager.logEvent(com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.STOP_LOCAL_SERVER)
        } catch (e: Exception) {
            Log.e("LocalServer", "Error stopping server", e)
        }
    }

    /**
     * Fetches link data from a remote sender via its QR transfer info and imports it into the local database.
     * @param qrTransferInfo Connection details for the sender.
     * @return [Result] indicating success or failure.
     */
    override suspend fun fetchAndImportFromSender(qrTransferInfo: QRTransferInfo): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response: HttpResponse =
                    httpClient.get {
                        url {
                            protocol = URLProtocol.HTTP
                            host = qrTransferInfo.ip
                            port = qrTransferInfo.port
                            path("api/links")
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

                val exportedData: List<LinkResponse> = response.body()

                importToDatabase(exportedData)

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Imports a list of [LinkResponse] into the local database.
     * @param links The list of links to import.
     */
    private fun importToDatabase(links: List<LinkResponse>) {
        deeprQueries.transaction {
            links.forEach { deeplink ->
                if (deeprQueries.getDeeprByLink(deeplink.link).executeAsList().isEmpty()) {
                    deeprQueries.importDeepr(
                        link = deeplink.link,
                        name = deeplink.name,
                        openedCount = deeplink.openedCount,
                        notes = deeplink.notes,
                        thumbnail = deeplink.thumbnail,
                        isFavourite = deeplink.isFavourite,
                        createdAt = deeplink.createdAt,
                        profileId = 1L, // Default profile
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
                }
            }
        }
    }

    /**
     * Generates a JSON string containing [QRTransferInfo] for QR code generation.
     * @param port The port the server is running on.
     * @return The JSON string or null if IP is unavailable or serialization fails.
     */
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

    /**
     * Retrieves the current IP address of the device.
     * Tries WiFi IP first, then falls back to other network interfaces.
     * @return The IP address string or null if not found.
     */
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

/**
 * Data class representing a link in API responses.
 */
@Serializable
data class LinkResponse(
    val id: Long,
    val link: String,
    val name: String,
    val createdAt: String,
    val openedCount: Long,
    val notes: String,
    val thumbnail: String,
    val isFavourite: Long,
    val tags: List<String>,
)

/**
 * Data class representing tag information in API requests.
 */
@Serializable
data class TagData(
    val id: Long,
    val name: String,
) {
    /**
     * Converts this [TagData] to a [Tags] database object.
     */
    fun toDbTag() = Tags(id, name)
}

/**
 * Data class representing a request to add a new link.
 */
@Serializable
data class AddLinkRequest(
    val link: String,
    val name: String,
    val notes: String = "",
    val tags: List<TagData> = emptyList(),
    val profileId: Long = 1L,
)

/**
 * Data class representing a profile in API responses.
 */
@Serializable
data class ProfileResponse(
    val id: Long,
    val name: String,
    val createdAt: String,
)

/**
 * Data class representing a request to add a new profile.
 */
@Serializable
data class AddProfileRequest(
    val name: String,
)

/**
 * Data class representing link metadata (title and thumbnail).
 */
@Serializable
data class LinkInfoResponse(
    val title: String?,
    val imageUrl: String?,
)

/**
 * Data class representing a successful API operation.
 */
@Serializable
data class SuccessResponse(
    val message: String,
)

/**
 * Data class representing an API error.
 */
@Serializable
data class ErrorResponse(
    val error: String,
)

/**
 * Data class representing a tag and its link count in API responses.
 */
@Serializable
data class TagResponse(
    val id: Long,
    val name: String,
    val count: Int,
)

/**
 * Data class containing connection details for transferring data between devices via QR code.
 */
@Serializable
data class QRTransferInfo(
    val ip: String,
    val port: Int,
    val appVersion: String,
)
