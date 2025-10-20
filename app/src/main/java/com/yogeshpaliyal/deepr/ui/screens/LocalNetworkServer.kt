package com.yogeshpaliyal.deepr.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.lightspark.composeqr.QrCodeView
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.server.LocalServerService
import com.yogeshpaliyal.deepr.viewmodel.LocalServerViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Copy
import compose.icons.tablericons.Server
import org.koin.androidx.compose.koinViewModel

data object LocalNetworkServer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocalNetworkServerScreen(
    backStack: SnapshotStateList<Any>,
    modifier: Modifier = Modifier,
    viewModel: LocalServerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    // Track if user wants to start the server (used for permission flow)
    var pendingStart by remember { mutableStateOf(false) }

    // Request notification permission for Android 13+
    val notificationPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
                if (pendingStart) {
                    pendingStart = false
                    LocalServerService.startService(context)
                }
            }
        } else {
            null
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.local_network_server))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
                    }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.back),
                            modifier = if (isRtl) Modifier.scale(-1f, 1f) else Modifier,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Server Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                TablerIcons.Server,
                                contentDescription = null,
                                tint =
                                    if (isRunning) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                            )
                            Text(
                                text = stringResource(R.string.server_status),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Switch(
                            checked = isRunning,
                            onCheckedChange = {
                                if (it) {
                                    // Check if notification permission is required and granted
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        notificationPermissionState?.status?.isGranted == false
                                    ) {
                                        pendingStart = true
                                        notificationPermissionState.launchPermissionRequest()
                                    } else {
                                        LocalServerService.startService(context)
                                    }
                                } else {
                                    LocalServerService.stopService(context)
                                }
                            },
                        )
                    }

                    Text(
                        text =
                            if (isRunning) {
                                stringResource(R.string.server_running)
                            } else {
                                stringResource(R.string.server_stopped)
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Server URL Card
            AnimatedVisibility(isRunning && serverUrl != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Server Address Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.server_address),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            // IP Address
                            val ipAddress = serverUrl?.substringAfter("//")?.substringBefore(":") ?: ""
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(8.dp),
                                        ).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.ip_address),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = ipAddress,
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                    )
                                </Column>
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, ipAddress)
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.copied_to_clipboard),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    },
                                ) {
                                    Icon(
                                        TablerIcons.Copy,
                                        contentDescription = stringResource(R.string.copy),
                                    )
                                }
                            }

                            // Full URL
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(8.dp),
                                        ).padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.server_url),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = serverUrl ?: "",
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                            ),
                                    )
                                </Column>
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, serverUrl ?: "")
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.copied_to_clipboard),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    },
                                ) {
                                    Icon(
                                        TablerIcons.Copy,
                                        contentDescription = stringResource(R.string.copy),
                                    )
                                }
                            }
                        }
                    }

                    // QR Code Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.scan_qr_code),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.scan_qr_code_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            QrCodeView(
                                data = serverUrl ?: "",
                                modifier = Modifier.size(200.dp),
                            )
                        }
                    }

                    // API Information Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.api_endpoints),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.api_endpoints_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(8.dp),
                                        ).padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ApiEndpointItem("GET", "/api/links", stringResource(R.string.api_get_links))
                                ApiEndpointItem("POST", "/api/links", stringResource(R.string.api_add_link))
                                ApiEndpointItem("GET", "/api/tags", stringResource(R.string.api_get_tags))
                                ApiEndpointItem("GET", "/api/link-info", stringResource(R.string.api_get_link_info))
                                ApiEndpointItem("GET", "/api/server-info", stringResource(R.string.api_get_server_info))
                            }
                        }
                    }
                }
            }

            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.how_to_use),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = stringResource(R.string.local_server_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(
    context: Context,
    text: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Server URL", text)
    clipboard.setPrimaryClip(clip)
}

@Composable
private fun ApiEndpointItem(
    method: String,
    path: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            color =
                when (method) {
                    "GET" -> MaterialTheme.colorScheme.primary
                    "POST" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                },
            modifier =
                Modifier
                    .background(
                        when (method) {
                            "GET" -> MaterialTheme.colorScheme.primaryContainer
                            "POST" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        RoundedCornerShape(4.dp),
                    ).padding(horizontal = 6.dp, vertical = 2.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = path,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                fontSize = 12.sp,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}
