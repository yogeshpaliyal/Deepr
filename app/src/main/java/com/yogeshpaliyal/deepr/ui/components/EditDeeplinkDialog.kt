package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.Deepr
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.normalizeLink

@Composable
fun EditDeeplinkDialog(
    deepr: Deepr,
    onDismiss: () -> Unit,
    onSave: (link: String, name: String) -> Unit,
) {
    var link by remember { mutableStateOf(deepr.link) }
    var name by remember { mutableStateOf(deepr.name) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Deeplink") },
        text = {
            Column {
                TextField(
                    value = link,
                    onValueChange = {
                        link = it
                        isError = false
                    },
                    label = {
                        Text("Deeplink")
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Please enter a valid deeplink",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    suffix =
                        if (link.isEmpty()) {
                            null
                        } else {
                            {
                                ClearInputIconButton(
                                    onClick = {
                                        link = ""
                                        isError = false
                                    },
                                )
                            }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = { Text("Name") },
                    suffix =
                        if (name.isEmpty()) {
                            null
                        } else {
                            {
                                ClearInputIconButton(
                                    onClick = { name = "" },
                                )
                            }
                        },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalizedLink = normalizeLink(link)
                    if (isValidDeeplink(normalizedLink)) {
                        onSave(normalizedLink, name)
                    } else {
                        isError = true
                    }
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
