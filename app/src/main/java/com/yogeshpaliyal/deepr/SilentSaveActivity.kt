package com.yogeshpaliyal.deepr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Activity that silently saves shared links without opening the main app UI.
 * This provides a quick "save and go" experience for users who want to bookmark
 * links without interrupting their workflow.
 */
class SilentSaveActivity : ComponentActivity() {
    private val linkRepository: LinkRepository by inject()
    private val preferenceDataStore: AppPreferenceDataStore by inject()
    private val deeprQueries: DeeprQueries by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Store application context and scope for use after activity finishes
        val appContext = applicationContext

        // Handle the shared link
        handleSharedLink(intent, appContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val appContext = applicationContext
        handleSharedLink(intent, appContext)
    }

    private fun handleSharedLink(
        intent: Intent,
        appContext: Context,
    ) {
        when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val title = intent.getStringExtra(Intent.EXTRA_TITLE)

                if (sharedText != null) {
                    val normalizedLink = normalizeLink(sharedText)
                    if (isValidDeeplink(normalizedLink)) {
                        saveLinkSilently(normalizedLink, title ?: "", appContext)
                    } else {
                        showToast(appContext, appContext.getString(R.string.invalid_link_silent_save))
                    }
                }
            }
        }
    }

    private fun saveLinkSilently(
        link: String,
        title: String,
        appContext: Context,
    ) {
        // Launch in application scope so it continues after activity finishes
        lifecycleScope.launch {
            try {
                val profileId = preferenceDataStore.getSilentSaveProfileId.first()

                // Check if link already exists
                val existingLink =
                    withContext(Dispatchers.IO) {
                        deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                    }
                if (existingLink != null) {
                    showToast(appContext, appContext.getString(R.string.link_already_exists))
                    return@launch
                }

                linkRepository.insertDeepr(
                    link = link,
                    name = title,
                    openedCount = 0,
                    notes = "",
                    thumbnail = "",
                    profileId = profileId,
                )

                showToast(appContext, appContext.getString(R.string.link_saved_silently))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save link silently: $link", e)
                showToast(appContext, appContext.getString(R.string.failed_to_save_link))
            }
        }
    }

    private fun showToast(
        context: Context,
        message: String,
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    companion object {
        private const val TAG = "SilentSaveActivity"
    }
}
