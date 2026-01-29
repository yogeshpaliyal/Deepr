package com.yogeshpaliyal.deepr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.yogeshpaliyal.deepr.data.LinkRepository
import com.yogeshpaliyal.deepr.preference.AppPreferenceDataStore
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the shared link
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
                        Toast.makeText(
                            this,
                            getString(R.string.invalid_link_silent_save),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
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
                linkRepository.insertDeepr(
                    link = link,
                    name = title,
                    openedCount = 0,
                    notes = "",
                    thumbnail = "",
                    profileId = profileId
                )

                Toast.makeText(
                    this@SilentSaveActivity,
                    getString(R.string.link_saved_silently),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@SilentSaveActivity,
                    getString(R.string.failed_to_save_link),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                finish()
            }
        }
    }
}
