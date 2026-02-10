package com.yogeshpaliyal.deepr.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.openDeeplink

@Composable
fun NoteViewDialog(
    deepr: GetLinksAndTags,
    onDismiss: () -> Unit,
    onEdit: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary

    // Build annotated string with clickable URLs
    val annotatedString =
        buildAnnotatedString {
            val text = deepr.notes
            append(text)

            // Find all URLs in the text using Patterns.WEB_URL
            val matcher = Patterns.WEB_URL.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                // Add URL annotation
                addStringAnnotation(
                    tag = "URL",
                    annotation = text.substring(start, end),
                    start = start,
                    end = end,
                )

                // Style the URL with primary color and underline
                addStyle(
                    style =
                        SpanStyle(
                            color = primaryColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    start = start,
                    end = end,
                )
            }
        }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.note))
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState),
            ) {
                SelectionContainer {
                    ClickableText(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        onClick = { offset ->
                            // Check if clicked position has a URL annotation
                            annotatedString
                                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()
                                ?.let { annotation ->
                                    // Open the URL using openDeeplink utility
                                    openDeeplink(context, annotation.item)
                                }
                        },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onEdit(deepr)
                },
            ) {
                Text(stringResource(R.string.edit))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(context.getString(R.string.note), deepr.notes)
                    clipboard.setPrimaryClip(clip)
                    Toast
                        .makeText(context, context.getString(R.string.note_copied), Toast.LENGTH_SHORT)
                        .show()
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.copy))
            }
        },
    )
}
