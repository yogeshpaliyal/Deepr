package com.yogeshpaliyal.deepr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yogeshpaliyal.deepr.ui.theme.DeeprTheme
import com.yogeshpaliyal.deepr.util.openDeeplink
import com.yogeshpaliyal.deepr.viewmodel.AccountViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AccountViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeeprTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val accounts by viewModel.accounts.collectAsState()
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val inputText = remember { mutableStateOf("") }
                        var isError by remember { mutableStateOf(false) }

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
                        val context = LocalContext.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(onClick = { viewModel.insertAccount(inputText.value) }) {
                                Text("Add to DB")
                            }
                            Button(onClick = {
                                isError = !openDeeplink(context, inputText.value)
                            }) {
                                Text("Execute")
                            }
                            Button(onClick = {
                                val success = openDeeplink(context, inputText.value)
                                if (success) {
                                    viewModel.insertAccount(inputText.value)
                                }
                                isError = !success
                            }) {
                                Text("Execute and Save")
                            }
                        }
                        AccountList(
                            accounts = accounts
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountList(modifier: Modifier = Modifier, accounts: List<Deepr>) {
    LazyColumn(modifier = modifier.padding(16.dp)) {
        if (accounts.isEmpty()) {
            item {
                Text(text = "No deeplinks found.")
            }
        } else {
            items(accounts) { account ->
                AccountItem(account = account)
            }
        }
    }
}

@Composable
fun AccountItem(modifier: Modifier = Modifier, account: Deepr) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = account.link)
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