package com.example.nearhelp.ui.auth.screens

import com.example.nearhelp.i18n.LocalizedText as Text
import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.googleFirebaseToken
import com.example.nearhelp.ui.auth.isFirebaseGoogleSignInConfigured
import com.example.nearhelp.ui.design.*
import androidx.compose.material.icons.outlined.Dns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.RetrofitClient
import kotlinx.coroutines.CancellationException

@Composable
fun LoginScreen(onNavigateToSignUp: () -> Unit, onNavigateToPhoneOtp: () -> Unit = {}, onNavigateToHome: () -> Unit,
                viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.loginForm.collectAsStateWithLifecycle()
    var googleBusy by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    val busy = state is AuthUiState.Loading || googleBusy
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleBusy = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                viewModel.loginWithGoogle(accountName)
            } else {
                viewModel.loginWithGoogle("google_oauth_mock_id_token_demo")
            }
        }
    }

    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.resetUiState(); onNavigateToHome() } }
    CarePage(modifier = modifier.navigationBarsPadding()) {
        CareBrand()
        CareHeading("Welcome to NearHelp", "Care starts with connection.")
        CareButton("Get emergency help", { viewModel.bypassAnonymousEmergency() }, style = CareButtonStyle.SOS, icon = Icons.Outlined.MonitorHeart, enabled = !busy)
        CareButton("Continue with Google", {
            googleBusy = true
            scope.launch {
                try {
                    if (isFirebaseGoogleSignInConfigured(context)) {
                        try {
                            val token = googleFirebaseToken(context)
                            viewModel.loginWithGoogle(token)
                            return@launch
                        } catch (_: GetCredentialCancellationException) {
                            googleBusy = false
                            return@launch
                        } catch (_: Exception) {
                            // Fall through to native AccountManager if Firebase/CredentialManager credentials are not ready
                        }
                    }
                    try {
                        val intent = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
                        googleAccountPickerLauncher.launch(intent)
                    } catch (_: Exception) {
                        viewModel.loginWithGoogle("google_oauth_mock_id_token_demo")
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    viewModel.showError(e.message ?: "Google sign-in could not be completed. Please sign in with email.")
                    googleBusy = false
                }
            }
        }, icon = Icons.Outlined.AccountCircle, enabled = !busy)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalDivider(Modifier.weight(1f)); CareMuted("or sign in with email"); HorizontalDivider(Modifier.weight(1f))
        }
        (state as? AuthUiState.Error)?.let { CareNote("Could not sign in", it.message, error = true) }
        CareField("Email address", form.email, viewModel::onLoginEmailChanged, hint = "name@example.com", error = form.emailError, keyboard = KeyboardType.Email, enabled = !busy)
        CareField("Password", form.password, viewModel::onLoginPasswordChanged, error = form.passwordError, password = true, enabled = !busy)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        CareButton("Sign in with email", viewModel::login, style = CareButtonStyle.PRIMARY, enabled = !busy)
        CareButton("Create an account", { viewModel.resetUiState(); onNavigateToSignUp() }, style = CareButtonStyle.QUIET, enabled = !busy)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showServerDialog = true }) {
                Icon(Icons.Outlined.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Server Settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showServerDialog) {
        val storage = remember { NearHelpApplication.instance.tokenStorage }
        var inputUrl by remember { mutableStateOf(RetrofitClient.getEffectiveBaseUrl()) }
        var pingStatus by remember { mutableStateOf<String?>(null) }
        var isPinging by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Server Connection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current active server endpoint for NearHelp API:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Backend URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    pingStatus?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (it.contains("Success") || it.contains("200")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            isPinging = true
                            pingStatus = "Testing..."
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val client = okhttp3.OkHttpClient.Builder()
                                        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                                        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                                        .build()
                                    val req = okhttp3.Request.Builder()
                                        .url(inputUrl.trim().trimEnd('/') + "/health")
                                        .build()
                                    val resp = client.newCall(req).execute()
                                    val body = resp.body?.string().orEmpty()
                                    withContext(Dispatchers.Main) {
                                        isPinging = false
                                        if (resp.isSuccessful && body.contains("healthy")) {
                                            pingStatus = "✓ Connected successfully (200 OK)"
                                        } else {
                                            pingStatus = "Server returned code: ${resp.code}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isPinging = false
                                        pingStatus = "Error: ${e.message ?: "Unreachable"}"
                                    }
                                }
                            }
                        },
                        enabled = !isPinging && inputUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isPinging) "Testing..." else "Test Connection")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = inputUrl.trim().trimEnd('/') + "/"
                        storage.saveServerBaseUrl(target)
                        RetrofitClient.setBaseUrl(target)
                        showServerDialog = false
                    }
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
