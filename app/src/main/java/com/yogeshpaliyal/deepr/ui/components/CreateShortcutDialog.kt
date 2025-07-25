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
import com.yogeshpaliyal.deepr.Deepr

@Composable
fun CreateShortcutDialog(deepr: Deepr, onDismiss: () -> Unit, onCreate: (Deepr, String) -> Unit) {
    var shortcutName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Shortcut") },
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
                onClick = { onCreate(deepr, shortcutName) },
                enabled = shortcutName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
