package com.yogeshpaliyal.deepr.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.yogeshpaliyal.deepr.BuildConfig
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.data.NetworkRepository
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.util.Locale

class TransferLinkLocalServerRepositoryImpl(
    private val context: Context,
    private val deeprQueries: DeeprQueries,
    private val accountViewModel: AccountViewModel,
    private val networkRepository: NetworkRepository,
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
            generateQRCode()
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
                    }
                }

            server?.start(wait = false)
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
    val links: List<LinkResponse>,
    val tags: List<TagData>,
)

@Serializable
data class QRTransferInfo(
    val ip: String,
    val port: Int,
    val appVersion: String,
)
