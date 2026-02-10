package com.yogeshpaliyal.deepr.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanOptions
import com.lightspark.composeqr.QrCodeView
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.server.LocalServerService
import com.yogeshpaliyal.deepr.server.LocalServerTransferLink
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.Screen
import com.yogeshpaliyal.deepr.util.QRScanner
import com.yogeshpaliyal.deepr.viewmodel.TransferLinkLocalServerViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Copy
import compose.icons.tablericons.Scan
import compose.icons.tablericons.Server
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

object TransferLinkLocalNetworkServer : Screen {
    @Composable
    override fun Content() {
        TransferLinkLocalServerScreen()
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransferLinkLocalServerScreen(
    modifier: Modifier = Modifier,
    viewModel: TransferLinkLocalServerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val backStack = LocalNavigator.current
    val localServerInstance = koinInject<LocalServerTransferLink>()
    val isRunning by localServerInstance.isRunning.collectAsStateWithLifecycle()
    val serverUrl by localServerInstance.serverUrl.collectAsStateWithLifecycle()
    val qrCodeData by localServerInstance.qrCodeData.collectAsStateWithLifecycle()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coroutine = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            localServerInstance.stopServer()
        }
    }

    LaunchedEffect(true) {
        viewModel.transferResultFlow.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val qrScanner =
        rememberLauncherForActivityResult(
            QRScanner(),
        ) { result ->
            if (result.contents == null) {
                Toast.makeText(context, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.import(result.contents)
            }
        }

    // Track if user wants to start the server (used for permission flow)
    var pendingStart by remember { mutableStateOf(false) }

    // Request notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
            if (pendingStart) {
                pendingStart = false
                LocalServerService.startService(context = context, port = 9000)
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
                    Text(stringResource(R.string.transfer_link_server))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backStack.removeLast()
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
                                coroutine.launch {
                                    if (it) {
                                        // Check if notification permission is required and granted
                                        localServerInstance.startServer(9000)
                                    } else {
                                        localServerInstance.stopServer()
                                    }
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
            AnimatedVisibility(
                visible = isRunning && (serverUrl != null),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
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
                                text = stringResource(R.string.server_url),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                            )

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
                                Text(
                                    text = serverUrl ?: "",
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                        ),
                                    modifier = Modifier.weight(1f),
                                )
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
                                data = qrCodeData ?: "",
                                modifier = Modifier.size(200.dp),
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
                        text = stringResource(R.string.transfer_link_server_instructions),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.scan_qr_to_get_data),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            qrScanner.launch(ScanOptions())
                        },
                    ) {
                        Icon(
                            TablerIcons.Scan,
                            contentDescription = stringResource(R.string.qr_scanner),
                        )
                    }
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
