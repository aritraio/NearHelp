package com.example.nearhelp.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.ClearCredentialStateRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Returns the Firebase ID token expected by the existing backend, never an account name. */
suspend fun googleFirebaseToken(context: Context): String {
    val clientIdResource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    check(clientIdResource != 0 && FirebaseApp.initializeApp(context) != null) {
        "Google sign-in is unavailable right now. Please sign in with email."
    }
    val clientId = context.getString(clientIdResource)
    val option = GetSignInWithGoogleOption.Builder(clientId).build()
    val response = CredentialManager.create(context).getCredential(context,
        GetCredentialRequest.Builder().addCredentialOption(option).build())
    val credential = response.credential
    check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        "Google sign-in did not return a valid credential. Please try again."
    }
    val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
    val auth = FirebaseAuth.getInstance()
    val signedIn = auth.signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null)).awaitResult()
    return signedIn.user?.getIdToken(true)?.awaitResult()?.token
        ?: error("Google sign-in could not be completed. Please try again.")
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (!continuation.isActive) return@addOnCompleteListener
        if (task.isSuccessful) continuation.resume(task.result)
        else continuation.resumeWithException(task.exception ?: IllegalStateException("Google sign-in was cancelled."))
    }
}

/** Local logout must work even when Google services are missing or unavailable. */
suspend fun clearGoogleAuthentication(context: Context) {
    runCatching { if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance().signOut() }
    try { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    catch (e: CancellationException) { throw e }
    catch (_: Exception) { /* The NearHelp session has already been removed. */ }
}
