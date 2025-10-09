package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R

@Composable
fun DeleteConfirmationDialog(
    deepr: GetLinksAndTags,
    onDismiss: () -> Unit,
    onConfirm: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.delete_confirmation_title))
        },
        text = {
            Text(stringResource(R.string.delete_confirmation_message))
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(deepr)
                onDismiss()
            }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
