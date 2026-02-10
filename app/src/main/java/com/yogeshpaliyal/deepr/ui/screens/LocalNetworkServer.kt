package com.yogeshpaliyal.deepr.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.lightspark.composeqr.QrCodeView
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.server.LocalServerService
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.Screen
import com.yogeshpaliyal.deepr.viewmodel.LocalServerViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Copy
import compose.icons.tablericons.DeviceMobile
import compose.icons.tablericons.Edit
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Qrcode
import compose.icons.tablericons.Server
import compose.icons.tablericons.Wifi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

object LocalNetworkServer : Screen {
    @Composable
    override fun Content() {
        LocalNetworkServerScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocalNetworkServerScreen(
    modifier: Modifier = Modifier,
    viewModel: LocalServerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val navigatorContext = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val serverPort by viewModel.serverPort.collectAsStateWithLifecycle()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coroutine = rememberCoroutineScope()

    // Track if user wants to start the server (used for permission flow)
    var pendingStart by remember { mutableStateOf(false) }

    // Port configuration dialog
    var showPortDialog by remember { mutableStateOf(false) }
    var portInput by remember { mutableStateOf("") }
    var portError by remember { mutableStateOf(false) }

    // Request notification permission for Android 13+
    val notificationPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
                if (pendingStart) {
                    pendingStart = false
                    LocalServerService.startService(context = context, port = serverPort)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.local_network_server))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigatorContext.removeLast()
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Server Status Card
            ServerSwitch(isRunning, serverPort, { pendingStart = it }, notificationPermissionState)

            // Port Configuration Card
            PortConfigurationCard(
                currentPort = serverPort,
                onChangePort = {
                    portInput = serverPort.toString()
                    portError = false
                    showPortDialog = true
                },
            )

            // Server Details Section
            AnimatedVisibility(
                visible = isRunning && serverUrl != null,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Full URL Card
                    ServerInfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.server_url),
                        value = serverUrl ?: "",
                        icon = TablerIcons.Wifi,
                        isLarge = true,
                        onCopy = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            copyToClipboard(context, serverUrl ?: "")
                            showCopiedToast(context)
                        },
                    )

                    // QR Code Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    TablerIcons.Qrcode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = stringResource(R.string.scan_qr_code),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = stringResource(R.string.scan_qr_code_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp),
                                        ).padding(16.dp),
                            ) {
                                QrCodeView(
                                    data = serverUrl ?: "",
                                    modifier = Modifier.size(180.dp),
                                    colors =
                                        com.lightspark.composeqr.QrCodeColors(
                                            background = Color.White,
                                            foreground = Color.Black,
                                        ),
                                )
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
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            TablerIcons.DeviceMobile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.how_to_use),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = stringResource(R.string.local_server_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // API Documentation Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            TablerIcons.InfoCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.api_endpoints),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp),
                                ).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ApiEndpointItem(
                            "GET",
                            "/api/links",
                            stringResource(R.string.api_get_links),
                        )
                        ApiEndpointItem(
                            "POST",
                            "/api/links",
                            stringResource(R.string.api_add_link),
                        )
                        ApiEndpointItem(
                            "GET",
                            "/api/tags",
                            stringResource(R.string.api_get_tags),
                        )
                        ApiEndpointItem(
                            "GET",
                            "/api/link-info",
                            stringResource(R.string.api_get_link_info),
                        )
                        ApiEndpointItem(
                            "GET",
                            "/api/server-info",
                            stringResource(R.string.api_get_server_info),
                        )
                    }
                }
            }
        }
    }

    // Port Configuration Dialog
    if (showPortDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPortDialog = false },
            title = { Text(stringResource(R.string.change_port)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.port_range_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = portInput,
                        onValueChange = {
                            portInput = it
                            portError = false
                        },
                        label = { Text(stringResource(R.string.port_number)) },
                        placeholder = { Text(stringResource(R.string.default_port)) },
                        isError = portError,
                        supportingText =
                            if (portError) {
                                { Text(stringResource(R.string.invalid_port)) }
                            } else {
                                null
                            },
                        keyboardOptions =
                            androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val port = portInput.toIntOrNull()
                        if (port != null && port in 1024..65535) {
                            viewModel.setServerPort(port)
                            showPortDialog = false
                            if (isRunning) {
                                coroutine.launch {
                                    LocalServerService.stopService(context)
                                    delay(200)
                                    LocalServerService.startService(context = context, port = port)
                                }
                            }
                            Toast
                                .makeText(
                                    context,
                                    if (isRunning) {
                                        context.getString(R.string.port_changed_restart)
                                    } else {
                                        context.getString(
                                            R.string.saved,
                                        )
                                    },
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else {
                            portError = true
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPortDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ServerSwitch(
    isRunning: Boolean = false,
    currentPort: Int = 8080,
    setPendingStart: (Boolean) -> Unit = {},
    notificationPermissionState: PermissionState? = null,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isRunning) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(
                            if (isRunning) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    TablerIcons.Server,
                    contentDescription = null,
                    tint =
                        if (isRunning) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.server_status),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text =
                        if (isRunning) {
                            stringResource(R.string.server_running)
                        } else {
                            stringResource(R.string.server_stopped)
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isRunning) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    fontWeight = if (isRunning) FontWeight.Medium else FontWeight.Normal,
                )
            }
            Switch(
                checked = isRunning,
                onCheckedChange = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (it) {
                        // Check if notification permission is required and granted
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            notificationPermissionState?.status?.isGranted == false
                        ) {
                            setPendingStart(true)
                            notificationPermissionState.launchPermissionRequest()
                        } else {
                            LocalServerService.startService(context = context, port = currentPort)
                        }
                    } else {
                        LocalServerService.stopService(context)
                    }
                },
            )
        }
    }
}

@Composable
private fun ServerInfoCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    onCopy: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .clickable { onCopy() },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Icon(
                    TablerIcons.Copy,
                    contentDescription = stringResource(R.string.copy),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = value,
                style =
                    if (isLarge) {
                        MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    },
                fontWeight = FontWeight.Medium,
                maxLines = if (isLarge) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun copyToClipboard(
    context: Context,
    text: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(context.getString(R.string.server_info), text)
    clipboard.setPrimaryClip(clip)
}

private fun showCopiedToast(context: Context) {
    Toast
        .makeText(
            context,
            context.getString(R.string.copied_to_clipboard),
            Toast.LENGTH_SHORT,
        ).show()
}

@Composable
private fun ApiEndpointItem(
    method: String,
    path: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier
                    .background(
                        when (method) {
                            "GET" -> MaterialTheme.colorScheme.primaryContainer
                            "POST" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        RoundedCornerShape(6.dp),
                    ).padding(horizontal = 8.dp, vertical = 4.dp)
                    .width(45.dp),
            textAlign = TextAlign.Center,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = path,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun PortConfigurationCard(
    currentPort: Int,
    onChangePort: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Icon(
                TablerIcons.Server,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.server_port),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$currentPort",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onChangePort()
                },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    TablerIcons.Edit,
                    contentDescription = stringResource(R.string.change_port),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
