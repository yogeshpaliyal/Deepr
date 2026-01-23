package com.yogeshpaliyal.deepr.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
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

@Composable
fun NoteViewDialog(
    deepr: GetLinksAndTags,
    onDismiss: () -> Unit,
    onEdit: (GetLinksAndTags) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

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
                ClickableLinkText(
                    text = deepr.notes,
                    onLinkClick = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_link), Toast.LENGTH_SHORT).show()
                        }
                    },
                )
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

@Composable
private fun ClickableLinkText(
    text: String,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary

    val annotatedString =
        buildAnnotatedString {
            append(text)
            val matcher = Patterns.WEB_URL.matcher(text)
            while (matcher.find()) {
                val url = matcher.group().trim()
                val start = matcher.start()
                val end = matcher.end()
                addStyle(
                    style =
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    start = start,
                    end = end,
                )
                addStringAnnotation(
                    tag = "URL",
                    annotation = url,
                    start = start,
                    end = end,
                )
            }
        }

    ClickableText(
        text = annotatedString,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString
                .getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    var url = annotation.item.trim()
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://$url"
                    }
                    onLinkClick(url)
                }
        },
    )
}
