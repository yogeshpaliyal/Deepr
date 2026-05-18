package com.yogeshpaliyal.deepr.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.ui.LocalNavigator
import com.yogeshpaliyal.deepr.ui.Screen
import com.yogeshpaliyal.deepr.ui.components.QrCodeDialog
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
fun LocalNetworkServerScreen() {
    val viewModel: LocalServerViewModel = koinViewModel()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val serverPort by viewModel.serverPort.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigatorContext = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var showQrDialog by remember { mutableStateOf(false) }

    val notificationPermissionState =
        rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS,
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.local_network_server))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigatorContext.removeLast()
                    }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = stringResource(R.string.back),
                            modifier = if (isRtl) Modifier.graphicsLayer(scaleX = -1f) else Modifier,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Main Server Status Card
            StatusCard(
                isRunning = isRunning,
                serverUrl = serverUrl,
                onToggle = {
                    if (it) {
                        if (notificationPermissionState.status.isGranted) {
                            viewModel.startServer(context)
                        } else {
                            notificationPermissionState.launchPermissionRequest()
                        }
                    } else {
                        viewModel.stopServer(context)
                    }
                },
                onQrClick = { showQrDialog = true },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Configuration Section
            Text(
                text = stringResource(R.string.configuration),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
            )

            PortConfigurationCard(
                port = serverPort,
                isRunning = isRunning,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // How it works / Info
            InfoSection()
        }
    }

    if (showQrDialog && serverUrl != null) {
        QrCodeDialog(
            url = serverUrl!!,
            onDismiss = { showQrDialog = false },
        )
    }
}

@Composable
fun StatusCard(
    isRunning: Boolean,
    serverUrl: String?,
    onToggle: (Boolean) -> Unit,
    onQrClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.outlinedCardColors(
                containerColor =
                    if (isRunning) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = if (isRunning) TablerIcons.Wifi else TablerIcons.Server,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRunning) stringResource(R.string.server_running) else stringResource(R.string.server_stopped),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            if (isRunning && serverUrl != null) {
                Text(
                    text = serverUrl,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp,
                        ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )

                Row(
                    modifier = Modifier.padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val sendIntent: Intent =
                                Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, serverUrl)
                                    type = "text/plain"
                                }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    ) {
                        Icon(TablerIcons.Copy, contentDescription = "Copy")
                    }

                    IconButton(
                        onClick = onQrClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    ) {
                        Icon(TablerIcons.Qrcode, contentDescription = "QR Code")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Switch(
                checked = isRunning,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
fun PortConfigurationCard(
    port: String,
    isRunning: Boolean,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    TablerIcons.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.server_port),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Text(
                text = port,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun InfoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InfoItem(
            icon = TablerIcons.DeviceMobile,
            text = "Connect from any browser on your local network to manage your links.",
        )
        InfoItem(
            icon = TablerIcons.InfoCircle,
            text = "Keep the app open and server running while using the web interface.",
        )
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
