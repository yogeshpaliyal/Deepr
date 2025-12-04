package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.shared.data.DeeprLink

@Composable
fun DeleteConfirmationDialog(
    deepr: DeeprLink,
    onDismiss: () -> Unit,
    onConfirm: (DeeprLink) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.delete_confirmation_title))
        },
        text = {
            val displayName = if (deepr.name.isNotEmpty()) deepr.name else deepr.link
            val message =
                buildAnnotatedString {
                    append("Are you sure you want to delete ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("'$displayName'")
                    }
                    append("?")
                }
            Text(
                text = message,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(deepr)
                    onDismiss()
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
            ) {
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
