package com.yogeshpaliyal.deepr.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yogeshpaliyal.deepr.R

@Composable
fun ExportSuccessDialog(
    message: String,
    uri: Uri?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_complete)) },
        text = { Text(message) },
        confirmButton = {
            if (uri != null) {
                Button(
                    onClick = {
                        try {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                // For Android Q and above, open the Downloads folder
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(
                                        DocumentsContract.buildDocumentUri(
                                            "com.android.externalstorage.documents",
                                            "primary:Download/Deepr"
                                        ),
                                        DocumentsContract.Document.MIME_TYPE_DIR
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            } else {
                                // For older versions, try to open the file location
                                Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "resource/folder")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                            
                            // Try to start the activity, fallback if no file manager is available
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                // Fallback: Try opening Downloads app
                                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                                    type = "resource/folder"
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(fallbackIntent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // If all else fails, just dismiss the dialog
                        }
                        onDismiss()
                    },
                ) {
                    Text(stringResource(R.string.open_file_location))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}
