package com.yogeshpaliyal.deepr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.ui.components.CreateShortcutDialog
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.createShortcut
import com.yogeshpaliyal.deepr.util.isValidDeeplink
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import com.yogeshpaliyal.deepr.viewmodel.SortOrder
import compose.icons.TablerIcons
import compose.icons.tablericons.Copy
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Filter
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search
import compose.icons.tablericons.Trash
import compose.icons.tablericons.X
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeeprTheme {
                var isSearchActive by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Text("Deepr")
                            },
                            actions = {
                                IconButton(onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) {
                                        searchQuery = ""
                                        viewModel.search("")
                                    }
                                }) {
                                    Icon(
                                        if (isSearchActive) TablerIcons.X else TablerIcons.Search,
                                        contentDescription = if (isSearchActive) "Close search" else "Search"
                                    )
                                }
                                FilterMenu(onSortOrderChange = {
                                    viewModel.setSortOrder(it)
                                })
                            }
                        )
                        AnimatedVisibility(visible = isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    viewModel.search(it)
                                },
                                placeholder = { Text("Search...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            )
                        }
                    }
                }) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .imePadding()
                    ) {
                        Content(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterMenu(onSortOrderChange: (SortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(TablerIcons.Filter, contentDescription = "Filter")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sort by Date Ascending") },
                onClick = {
                    onSortOrderChange(SortOrder.ASC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Date Descending") },
                onClick = {
                    onSortOrderChange(SortOrder.DESC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Ascending") },
                onClick = {
                    onSortOrderChange(SortOrder.OPENED_ASC)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Opened Descending") },
                onClick = {
                    onSortOrderChange(SortOrder.OPENED_DESC)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun Content(viewModel: AccountViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    Column {
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
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            accounts = accounts,
            onItemClick = {
                viewModel.incrementOpenedCount(it.id)
                openDeeplink(context, it.link)
            },
            onRemoveClick = {
                viewModel.deleteAccount(it.id)
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            },
            onShortcutClick = {
                showShortcutDialog = it
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
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
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(onClick = {
                        if (isValidDeeplink(inputText.value)) {
                            viewModel.insertAccount(inputText.value, false)
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT)
                                .show()
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
                                viewModel.insertAccount(inputText.value, true)
                                Toast.makeText(
                                    context,
                                    "Saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                                inputText.value = ""
                            }
                            isError = !success
                        } else {
                            isError = true
                        }
                    }) {
                        Text("Save & Execute")
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
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(vertical = 8.dp)) {
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
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onItemClick(account) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = account.link,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDateTime(account.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Opened: ${account.openedCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(TablerIcons.DotsVertical, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy link") },
                        onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Link copied", account.link)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                TablerIcons.Copy,
                                contentDescription = "Copy link"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add shortcut") },
                        onClick = {
                            onShortcutClick(account)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                TablerIcons.Plus,
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
                                TablerIcons.Trash,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val dbFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dbFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val date = dbFormatter.parse(dateTimeString)
        val displayFormatter =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        date?.let { displayFormatter.format(it) } ?: dateTimeString
    } catch (_: Exception) {
        dateTimeString // fallback to raw string
    }
}
