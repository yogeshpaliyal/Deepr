package com.yogeshpaliyal.deepr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.viewmodel.LocalServerViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Server
import org.koin.androidx.compose.koinViewModel

@Composable
fun ServerStatusBar(
    onServerStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocalServerViewModel = koinViewModel(),
) {
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()

    if (isRunning) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4CAF50))
                    .clickable { onServerStatusClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                TablerIcons.Server,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.server_running_tap_to_configure),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            if (serverUrl != null) {
                Text(
                    text = serverUrl!!.substringAfter("://"),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}
