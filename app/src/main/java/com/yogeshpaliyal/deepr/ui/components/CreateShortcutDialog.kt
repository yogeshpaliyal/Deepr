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
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.util.createShortcut
import com.yogeshpaliyal.deepr.util.getShortcut
import com.yogeshpaliyal.deepr.util.isShortcutSupported

@Composable
fun CreateShortcutDialog(deepr: Deepr, onDismiss: () -> Unit, onCreate: (Deepr, String) -> Unit) {
    val context = LocalContext.current
    val existingShortcut = getShortcut(context, deepr.id)
    if (isShortcutSupported(context)) {
        var shortcutName by remember {
            mutableStateOf(
                existingShortcut?.shortLabel?.toString() ?: ""
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
                    placeholder = { Text(text = deepr.link) }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreate(deepr, shortcutName)
                        createShortcut(context, deepr, shortcutName, existingShortcut != null)
                    },
                    enabled = shortcutName.isNotBlank()
                ) {
                    Text(if (existingShortcut == null) "Create" else "Edit")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
