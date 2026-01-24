package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes

data class SignInResult(
    val account: GoogleSignInAccount?,
    val error: SignInError?,
)

enum class SignInError {
    CANCELLED,
    PERMISSIONS_DENIED,
    NETWORK_ERROR,
    UNKNOWN_ERROR,
}

object GoogleDriveHelper {
    private const val TAG = "GoogleDriveHelper"
    private const val RC_SIGN_IN = 9001

    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(data: Intent?): SignInResult {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful for: ${account?.email}")
            SignInResult(account, null)
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed with code: ${e.statusCode}", e)
            val error =
                when (e.statusCode) {
                    12501 -> SignInError.CANCELLED // User cancelled
                    12502 -> SignInError.NETWORK_ERROR // Network error
                    else -> SignInError.UNKNOWN_ERROR
                }
            SignInResult(null, error)
        }
    }

    fun hasDrivePermissions(context: Context): Boolean {
        val account = getSignedInAccount(context)
        return account != null &&
            GoogleSignIn.hasPermissions(
                account,
                Scope(DriveScopes.DRIVE_FILE),
            )
    }

    /**
     * Check if user is signed in to Google (regardless of Drive permissions)
     */
    fun isGoogleSignedIn(context: Context): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

    /**
     * Check if user is fully authenticated with Drive permissions
     */
    fun isDriveAuthenticated(context: Context): Boolean = isGoogleSignedIn(context) && hasDrivePermissions(context)

    fun getSignedInAccount(context: Context): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Sign out from Google account completely
     */
    fun signOut(
        context: Context,
        onComplete: (() -> Unit)? = null,
    ) {
        val signInClient = getSignInClient(context)
        signInClient.signOut().addOnCompleteListener {
            onComplete?.invoke()
        }
    }
}
