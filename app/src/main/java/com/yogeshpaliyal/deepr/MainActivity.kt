package com.yogeshpaliyal.deepr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.createShortcut
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.widget.Toast

class MainActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeeprTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val accounts by viewModel.accounts.collectAsState()
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                            .imePadding()
                    ) {
                        val inputText = remember { mutableStateOf("") }
                        var isError by remember { mutableStateOf(false) }
                        val context = LocalContext.current
                        var showShortcutDialog by remember { mutableStateOf<Deepr?>(null) }

                        showShortcutDialog?.let { deepr ->
                            CreateShortcutDialog(
                                deepr = deepr,
                                onDismiss = { showShortcutDialog = null },
                                onCreate = { d, name ->
                                    createShortcut(context, d, name)
                                    showShortcutDialog = null
                                }
                            )
                        }

                        DeeprList(
                            modifier = Modifier.weight(1f),
                            accounts = accounts,
                            onItemClick = {
                                openDeeplink(context, it.link)
                            },
                            onRemoveClick = {
                                viewModel.deleteAccount(it.id)
                            },
                            onShortcutClick = {
                                showShortcutDialog = it
                            }
                        )

                        TextField(
                            value = inputText.value,
                            onValueChange = {
                                inputText.value = it
                                isError = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("Enter deeplink or command") },
                            isError = isError,
                            supportingText = {
                                if (isError) {
                                    Text(text = "Invalid or empty deeplink.")
                                }
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            OutlinedButton(onClick = {
                                if (isValidDeeplink(inputText.value)) {
                                    viewModel.insertAccount(inputText.value)
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                    inputText.value = ""
                                } else {
                                    isError = true
                                }
                            }) {
                                Text("Save")
                            }
                            OutlinedButton(onClick = {
                                isError = !openDeeplink(context, inputText.value)
                            }) {
                                Text("Execute")
                            }
                            Button(onClick = {
                                if (isValidDeeplink(inputText.value)) {
                                    val success = openDeeplink(context, inputText.value)
                                    if (success) {
                                        viewModel.insertAccount(inputText.value)
                                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                        inputText.value = ""
                                    }
                                    isError = !success
                                } else {
                                    isError = true
                                }
                            }){
                                Text("Save & Execute")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeeprList(
    modifier: Modifier = Modifier,
    accounts: List<Deepr>,
    onItemClick: (Deepr) -> Unit,
    onRemoveClick: (Deepr) -> Unit,
    onShortcutClick: (Deepr) -> Unit
) {
    LazyColumn(modifier = modifier) {
        if (accounts.isEmpty()) {
            item {
                Text(text = "No deeplinks found.")
            }
        } else {
            items(accounts) { account ->
                DeeprItem(
                    account = account,
                    onItemClick = onItemClick,
                    onRemoveClick = onRemoveClick,
                    onShortcutClick = onShortcutClick
                )
            }
        }
    }
}

@Composable
fun DeeprItem(
    modifier: Modifier = Modifier,
    account: Deepr,
    onItemClick: (Deepr) -> Unit,
    onRemoveClick: (Deepr) -> Unit,
    onShortcutClick: (Deepr) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick(account) }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = account.link,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add shortcut") },
                        onClick = {
                            onShortcutClick(account)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add shortcut"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onRemoveClick(account)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DeeprTheme {
        Greeting("Android")
    }
}