package com.yogeshpaliyal.deepr.google_drive

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

object GoogleDriveHelper {
    fun getSignInIntent(context: Context): Intent = getSignInClient(context).signInIntent

    fun getSignInClient(context: Context): GoogleSignInClient {
        val signInOptions =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    fun handleSignInResult(
        context: Context,
        data: Intent?,
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            // You can now use the account to access Google Drive
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    fun isDriveAuthenticated(context: Context): Boolean = getSignedInAccount(context) != null

    fun getSignedInAccount(context: Context): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    fun signOut(context: Context) {
        getSignInClient(context).signOut()
    }
}

private const val APP_NAME = "Deepr"

fun Context.getGoogleDrive(): Drive? {
    val account = GoogleDriveHelper.getSignedInAccount(this) ?: return null
    val credential =
        GoogleAccountCredential.usingOAuth2(
            this,
            listOf(DriveScopes.DRIVE_FILE),
        )
    credential.selectedAccount = account.account
    return Drive
        .Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            setHttpTimeout(credential),
        ).setApplicationName(APP_NAME)
        .build()
}

private fun setHttpTimeout(requestInitializer: HttpRequestInitializer): HttpRequestInitializer =
    HttpRequestInitializer { httpRequest ->
        requestInitializer.initialize(httpRequest)
        httpRequest.setConnectTimeout(3 * 60000) // 3 minutes connect timeout
        httpRequest.setReadTimeout(3 * 60000) // 3 minutes read timeout
    }
