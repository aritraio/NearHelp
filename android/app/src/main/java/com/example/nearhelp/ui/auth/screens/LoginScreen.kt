package com.example.nearhelp.ui.auth.screens

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
import com.example.nearhelp.ui.design.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@Composable
fun LoginScreen(onNavigateToSignUp: () -> Unit, onNavigateToPhoneOtp: () -> Unit = {}, onNavigateToHome: () -> Unit,
                viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.loginForm.collectAsStateWithLifecycle()
    var googleBusy by remember { mutableStateOf(false) }
    val busy = state is AuthUiState.Loading || googleBusy
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.resetUiState(); onNavigateToHome() } }
    CarePage(modifier = modifier.navigationBarsPadding()) {
        CareBrand()
        CareHeading("Welcome to NearHelp", "Care starts with connection.")
        CareButton("Get emergency help", { viewModel.bypassAnonymousEmergency() }, style = CareButtonStyle.SOS, icon = Icons.Outlined.MonitorHeart, enabled = !busy)
        CareButton("Continue with Google", {
            googleBusy = true
            scope.launch {
                try { viewModel.loginWithGoogle(googleFirebaseToken(context)) }
                catch (_: GetCredentialCancellationException) { /* Dismissing the account picker leaves email sign-in available. */ }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { viewModel.showError(e.message ?: "Google sign-in is unavailable. Please sign in with email.") }
                finally { googleBusy = false }
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
    }
}
