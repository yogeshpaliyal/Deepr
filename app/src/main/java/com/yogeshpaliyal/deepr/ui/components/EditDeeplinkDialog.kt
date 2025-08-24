package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun EditDeeplinkDialog(
    deepr: Deepr,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var link by remember { mutableStateOf(deepr.link) }
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
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Edit your deeplink URL",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidDeeplink(link)) {
                        onSave(link)
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
