package com.yogeshpaliyal.deepr

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

        // Handle the shared link
        handleSharedLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedLink(intent)
    }

    private fun handleSharedLink(intent: Intent) {
        when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val title = intent.getStringExtra(Intent.EXTRA_TITLE)

                if (sharedText != null) {
                    val normalizedLink = normalizeLink(sharedText)
                    if (isValidDeeplink(normalizedLink)) {
                        saveLinkSilently(normalizedLink, title ?: "")
                    } else {
                        showToastAndFinish(getString(R.string.invalid_link_silent_save))
                    }
                } else {
                    finish()
                }
            }
            else -> {
                finish()
            }
        }
    }

    private fun saveLinkSilently(link: String, title: String) {
        lifecycleScope.launch {
            try {
                val profileId = preferenceDataStore.getSelectedProfileId.first()

                // Check if link already exists
                val existingLink = deeprQueries.getDeeprByLink(link).executeAsOneOrNull()
                if (existingLink != null) {
                    showToastAndFinish(getString(R.string.link_already_exists))
                    return@launch
                }

                linkRepository.insertDeepr(
                    link = link,
                    name = title,
                    openedCount = 0,
                    notes = "",
                    thumbnail = "",
                    profileId = profileId
                )

                showToastAndFinish(getString(R.string.link_saved_silently))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save link silently: $link", e)
                showToastAndFinish(getString(R.string.failed_to_save_link))
            }
        }
    }

    private suspend fun showToastAndFinish(message: String) {
        Toast.makeText(
            this@SilentSaveActivity,
            message,
            Toast.LENGTH_SHORT
        ).show()
        // Delay to allow toast to be visible before finishing
        delay(TOAST_DELAY_MS)
        finish()
    }

    companion object {
        private const val TAG = "SilentSaveActivity"
        private const val TOAST_DELAY_MS = 500L
    }
}
