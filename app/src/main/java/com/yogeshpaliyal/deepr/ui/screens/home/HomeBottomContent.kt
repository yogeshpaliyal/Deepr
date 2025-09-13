package com.yogeshpaliyal.deepr.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.R
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeBottomContent(
    hazeState: HazeState,
    deeprQueries: DeeprQueries,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
    saveDialogInfo: SaveDialogInfo? = null,
    onSaveDialogInfoChange: ((SaveDialogInfo?) -> Unit) = {},
) {
    val inputText = remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    saveDialogInfo?.let { localSaveDialogInfo ->
        SaveCompleteDialog(localSaveDialogInfo) { result ->
            if (result != null) {
                if (result.executeAfterSave) {
                    openDeeplink(context, inputText.value, deeprQueries)
                }
                if (!isError) {
                    inputText.value = ""
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                    viewModel.insertAccount(result.link, result.name, result.executeAfterSave)
                }
            }
            onSaveDialogInfoChange(null)
        }
    }

    Column(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                    ),
                ).hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin(),
                ).fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
                    .imePadding()
                    .padding(8.dp),
        ) {
            TextField(
                value = inputText.value,
                onValueChange = {
                    inputText.value = it
                    isError = false
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                placeholder = { Text(stringResource(R.string.enter_deeplink_command)) },
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(text = stringResource(R.string.invalid_empty_deeplink))
                    }
                },
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                OutlinedButton(onClick = {
                    if (isValidDeeplink(inputText.value)) {
                        if (deeprQueries
                                .getDeeprByLink(inputText.value)
                                .executeAsOneOrNull() != null
                        ) {
                            Toast
                                .makeText(context, "Deeplink already exists", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            onSaveDialogInfoChange(SaveDialogInfo(inputText.value, false))
                        }
                    } else {
                        isError = true
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
                OutlinedButton(onClick = {
                    isError = !openDeeplink(context, inputText.value, deeprQueries)
                }) {
                    Text(stringResource(R.string.execute))
                }
                Button(onClick = {
                    if (isValidDeeplink(inputText.value)) {
                        if (deeprQueries
                                .getDeeprByLink(inputText.value)
                                .executeAsOneOrNull() != null
                        ) {
                            Toast
                                .makeText(context, "Deeplink already exists", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            onSaveDialogInfoChange(SaveDialogInfo(inputText.value, true))
                        }
                    } else {
                        isError = true
                    }
                }) {
                    Text(stringResource(R.string.save_and_execute))
                }
            }
        }
    }
}
