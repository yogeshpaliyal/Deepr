package com.yogeshpaliyal.deepr.update

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.yogeshpaliyal.deepr.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Checks the latest GitHub release of Deepr and, when it is newer than the installed
 * version, downloads its APK and hands it to the system package installer.
 */
class GithubAppUpdateManager(
    context: Context,
) : AppUpdateManager {
    private val appContext = context.applicationContext

    // Own client: the shared one keeps CIO's default 15s request timeout, which is too
    // short for downloading an APK on a slow connection.
    private val httpClient by lazy {
        HttpClient(CIO) {
            engine { requestTimeout = 0 }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    override suspend fun checkForUpdate(activity: Activity): UpdateCheckResult =
        try {
            val release =
                withContext(Dispatchers.IO) {
                    val response =
                        httpClient.get(LATEST_RELEASE_URL) {
                            header(HttpHeaders.Accept, "application/vnd.github+json")
                        }
                    if (!response.status.isSuccess()) {
                        throw IOException("GitHub returned ${response.status}")
                    }
                    response.body<GithubRelease>()
                }
            if (!release.draft &&
                !release.prerelease &&
                isNewerVersion(release.tagName, BuildConfig.VERSION_NAME)
            ) {
                val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                UpdateCheckResult.Available(
                    AvailableUpdate(
                        versionKey = release.tagName,
                        versionName = release.tagName.removePrefix("v"),
                        releaseNotes = release.body?.takeIf { it.isNotBlank() },
                        downloadUrl = apk?.downloadUrl,
                        releasePageUrl = release.htmlUrl,
                    ),
                )
            } else {
                UpdateCheckResult.UpToDate
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e)
        }

    override suspend fun startUpdate(
        activity: Activity,
        update: AvailableUpdate,
        onProgress: (Float?) -> Unit,
    ) {
        val downloadUrl = update.downloadUrl
        if (downloadUrl == null) {
            // No APK attached to the release; let the user grab it from the release page.
            update.releasePageUrl?.let {
                activity.startActivity(Intent(Intent.ACTION_VIEW, it.toUri()))
            }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !activity.packageManager.canRequestPackageInstalls()
        ) {
            activity.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    "package:${activity.packageName}".toUri(),
                ),
            )
            throw InstallPermissionRequiredException()
        }

        val apkFile = downloadApk(downloadUrl, onProgress)
        installApk(activity, apkFile)
    }

    private suspend fun downloadApk(
        url: String,
        onProgress: (Float?) -> Unit,
    ): File =
        withContext(Dispatchers.IO) {
            val dir = File(appContext.cacheDir, UPDATES_DIR).apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val target = File(dir, APK_FILE_NAME)
            val temp = File(dir, "$APK_FILE_NAME.part")

            httpClient.prepareGet(url).execute { response ->
                if (!response.status.isSuccess()) {
                    throw IOException("Download failed: ${response.status}")
                }
                val total = response.contentLength()?.takeIf { it > 0 }
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L
                temp.outputStream().use { output ->
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer, 0, buffer.size)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress(total?.let { downloaded.toFloat() / it })
                    }
                }
            }
            if (!temp.renameTo(target)) {
                throw IOException("Unable to save the downloaded update")
            }
            target
        }

    private fun installApk(
        activity: Activity,
        apkFile: File,
    ) {
        val uri =
            FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.updates.fileprovider",
                apkFile,
            )
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        activity.startActivity(intent)
    }

    @Serializable
    private data class GithubRelease(
        @SerialName("tag_name") val tagName: String,
        @SerialName("html_url") val htmlUrl: String? = null,
        val body: String? = null,
        val draft: Boolean = false,
        val prerelease: Boolean = false,
        val assets: List<GithubAsset> = emptyList(),
    )

    @Serializable
    private data class GithubAsset(
        val name: String,
        @SerialName("browser_download_url") val downloadUrl: String,
    )

    private companion object {
        const val LATEST_RELEASE_URL = "https://api.github.com/repos/yogeshpaliyal/Deepr/releases/latest"
        const val UPDATES_DIR = "updates"
        const val APK_FILE_NAME = "deepr-update.apk"
    }
}
