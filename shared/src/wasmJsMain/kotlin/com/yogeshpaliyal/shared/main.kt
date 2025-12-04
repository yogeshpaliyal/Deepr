package com.yogeshpaliyal.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport


// Dummy ui for web, hosted in github pages, imma make it to a landing page or something
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {

        Box {
            Text("Hello World WasmJs")
        }
    }
}