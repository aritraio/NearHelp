package com.example.nearhelp.ui.auth.screens

import com.example.nearhelp.i18n.LocalizedText as Text
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.design.*

@Composable
fun SignUpScreen(onNavigateToLogin: () -> Unit, onNavigateToHome: () -> Unit, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.signUpForm.collectAsStateWithLifecycle()
    val busy = state is AuthUiState.Loading
    LaunchedEffect(state) { if (state is AuthUiState.Success) { viewModel.resetUiState(); onNavigateToHome() } }
    CarePage("Create account", onNavigateToLogin, modifier.navigationBarsPadding()) {
        CareHeading("Create your account", "Your details help people reach you.")
        (state as? AuthUiState.Error)?.let { CareNote("Could not create account", it.message, error = true) }
        CareField("Full name", form.name, viewModel::onSignUpNameChanged, error = form.nameError, enabled = !busy)
        CareField("Email address", form.email, viewModel::onSignUpEmailChanged, error = form.emailError, keyboard = KeyboardType.Email, enabled = !busy)
        CareField("Password", form.password, viewModel::onSignUpPasswordChanged, hint = "At least 6 characters", error = form.passwordError, password = true, enabled = !busy)
        CareField("Contact phone · optional", form.phone, viewModel::onSignUpPhoneChanged, hint = "For emergency contact purposes", error = form.phoneError, keyboard = KeyboardType.Phone, enabled = !busy)
        CareSelect("Blood group", form.bloodGroup ?: "Choose blood group", listOf("A+", "A−", "B+", "B−", "AB+", "AB−", "O+", "O−"), viewModel::onSignUpBloodGroupSelected)
        CareCheck("I agree to the emergency response terms and Good Samaritan protocols.", form.termsAccepted, viewModel::onSignUpTermsToggled, !busy)
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        CareButton("Create account", viewModel::signUp, style = CareButtonStyle.PRIMARY, enabled = !busy)
        CareButton("Already have an account? Sign in", onNavigateToLogin, style = CareButtonStyle.QUIET, enabled = !busy)
    }
}
