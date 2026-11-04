package com.yogeshpaliyal.deepr.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.viewmodel.AppUpdateMessage
import com.yogeshpaliyal.deepr.viewmodel.AppUpdateUiState
import com.yogeshpaliyal.deepr.viewmodel.AppUpdateViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.CloudDownload
import org.koin.androidx.compose.koinViewModel

/**
 * "Check for updates" settings row. Uses the Play In-App Updates API on Play Store builds
 * and GitHub releases on the other builds (see AppUpdateManagerFactory per flavor).
 */
@Composable
fun AppUpdateSettingsItem(
    modifier: Modifier = Modifier,
    viewModel: AppUpdateViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            val text =
                when (message) {
                    AppUpdateMessage.UP_TO_DATE -> R.string.app_up_to_date
                    AppUpdateMessage.CHECK_FAILED -> R.string.update_check_failed
                    AppUpdateMessage.UPDATE_FAILED -> R.string.update_failed
                    AppUpdateMessage.INSTALL_PERMISSION_REQUIRED -> R.string.update_install_permission_required
                }
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    SettingsItem(
        icon = TablerIcons.CloudDownload,
        title = stringResource(R.string.check_for_updates),
        description = stringResource(R.string.check_for_updates_description),
        modifier = modifier,
        trailing =
            if (state == AppUpdateUiState.Checking) {
                { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
            } else {
                null
            },
        onClick = {
            activity?.let { viewModel.checkForUpdate(it) }
        },
    )

    when (val current = state) {
        is AppUpdateUiState.Available -> {
            val update = current.update
            AlertDialog(
                onDismissRequest = viewModel::dismiss,
                title = { Text(stringResource(R.string.update_available)) },
                text = {
                    Column {
                        Text(
                            update.versionName?.let {
                                stringResource(R.string.update_available_version, it)
                            } ?: stringResource(R.string.update_available_generic),
                        )
                        update.releaseNotes?.let { notes ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                notes,
                                style = MaterialTheme.typography.bodySmall,
                                modifier =
                                    Modifier
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        activity?.let { viewModel.startUpdate(it, update) }
                    }) {
                        Text(stringResource(R.string.update_now))
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismiss) {
                        Text(stringResource(R.string.update_later))
                    }
                },
            )
        }

        is AppUpdateUiState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.downloading_update)) },
                text = {
                    val progress = current.progress
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {},
            )
        }

        else -> {}
    }
}
