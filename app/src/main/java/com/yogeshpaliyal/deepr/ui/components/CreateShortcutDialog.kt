package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.analytics.AnalyticsManager
import com.yogeshpaliyal.deepr.util.createShortcut
import com.yogeshpaliyal.deepr.util.getShortcut
import com.yogeshpaliyal.deepr.util.isShortcutSupported
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CreateShortcutDialog(
    deepr: GetLinksAndTags,
    onDismiss: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
    analyticsManager: AnalyticsManager = koinInject(),
) {
    val context = LocalContext.current
    val existingShortcut = getShortcut(context, deepr.id)
    // Collect the shortcut icon preference state
    val useLinkBasedIcons by viewModel.useLinkBasedIcons.collectAsStateWithLifecycle()

    if (isShortcutSupported(context)) {
        var shortcutName by remember {
            mutableStateOf(
                existingShortcut?.shortLabel?.toString() ?: deepr.name,
            )
        }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("${if (existingShortcut == null) "Create" else "Edit"} Shortcut") },
            text = {
                TextField(
                    value = shortcutName,
                    onValueChange = { shortcutName = it },
                    label = { Text("Shortcut Name") },
                    placeholder = { Text(text = deepr.link) },
                    suffix =
                        if (shortcutName.isEmpty()) {
                            null
                        } else {
                            {
                                ClearInputIconButton(
                                    onClick = {
                                        shortcutName = ""
                                    },
                                )
                            }
                        },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                        createShortcut(
                            context,
                            deepr,
                            shortcutName,
                            existingShortcut != null,
                            useLinkBasedIcons,
                        )
                        analyticsManager.logEvent(
                            com.yogeshpaliyal.deepr.analytics.AnalyticsEvents.CREATE_SHORTCUT,
                            mapOf(com.yogeshpaliyal.deepr.analytics.AnalyticsParams.LINK_ID to deepr.id),
                        )
                    },
                    enabled = shortcutName.isNotBlank(),
                ) {
                    Text(if (existingShortcut == null) "Create" else "Edit")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    }
}
