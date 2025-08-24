package com.yogeshpaliyal.deepr.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

data class SaveDialogInfo(
    val link: String,
    val executeAfterSave: Boolean,
)

data class SaveDialogSuccessInfo(
    val executeAfterSave: Boolean,
    val link: String,
    val name: String,
)

@Composable
fun SaveCompleteDialog(
    localSaveDialogInfo: SaveDialogInfo,
    modifier: Modifier = Modifier,
    onDismiss: (result: SaveDialogSuccessInfo?) -> Unit,
) {
    val linkName = remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            onDismiss(null)
        },
        modifier = modifier,
        title = {
            Text("Save Deeplink")
        },
        text = {
            Text("Save the deeplink: ${localSaveDialogInfo.link} ?")
            TextField(
                value = linkName.value,
                onValueChange = {
                    linkName.value = it
                    isError = false
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                placeholder = { Text("Please enter name for the link") },
                supportingText = {
                    if (isError) {
                        Text(text = "Please enter name for the link.")
                    }
                },
            )
        },
        confirmButton = {
            Button(onClick = {
                if (linkName.value.trim().isBlank()) {
                    isError = true
                    return@Button
                }
                onDismiss(SaveDialogSuccessInfo(localSaveDialogInfo.executeAfterSave, localSaveDialogInfo.link, linkName.value))
            }) {
                Text(if (localSaveDialogInfo.executeAfterSave) "Save & Execute" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                onDismiss(null)
            }) {
                Text("Cancel")
            }
        },
    )
}
