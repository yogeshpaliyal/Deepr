package com.yogeshpaliyal.deepr.security

import androidx.compose.runtime.mutableStateOf

/**
 * Process-scoped unlock flag for the optional app-lock feature. Reset to false whenever the
 * whole app leaves the foreground (see ProcessLifecycleOwner wiring in DeeprApplication), so
 * re-authentication is required each time the app is brought back, and naturally starts locked
 * on a fresh process/cold start.
 */
object AppLockState {
    val isUnlocked = mutableStateOf(false)
}
