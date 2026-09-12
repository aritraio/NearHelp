package com.example.nearhelp.ui.auth.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimDivider
import com.example.nearhelp.theme.VictimInputBg
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.components.EmergencyButton
import com.example.nearhelp.ui.victim.NearHelpBrandHeader

/**
 * Victim Login — matches ui/demo-ui-1st/victim mockup:
 * Skip to demo, heart logo, One-Tap SOS pink card, Google button,
 * OR divider, Email/Password fields (16dp, #F1F5F9), red Login CTA.
 */
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToPhoneOtp: () -> Unit = {},
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loginForm by viewModel.loginForm.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var passwordVisible by remember { mutableStateOf(false) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) viewModel.loginWithGoogle(accountName)
            else viewModel.loginWithGoogle("google_oauth_mock_id_token_demo")
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onNavigateToHome()
            viewModel.resetUiState()
        }
    }

    val isAuthLoading = uiState is AuthUiState.Loading
    val isEmergencyLoading = isAuthLoading && (uiState as? AuthUiState.Loading)?.message?.contains("Emergency", ignoreCase = true) == true
    val isGoogleLoading = isAuthLoading && (uiState as? AuthUiState.Loading)?.message?.contains("Google", ignoreCase = true) == true
    val isEmailLoginLoading = isAuthLoading && !isEmergencyLoading && !isGoogleLoading

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VictimBorder,
        unfocusedBorderColor = VictimBorder,
        focusedContainerColor = VictimInputBg,
        unfocusedContainerColor = VictimInputBg,
        cursorColor = VictimPrimary,
        focusedTextColor = VictimTextDark,
        unfocusedTextColor = VictimTextDark,
        focusedPlaceholderColor = Color(0xFF94A3B8),
        unfocusedPlaceholderColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = VictimTextMuted,
        unfocusedLeadingIconColor = VictimTextMuted,
    )

    Box(modifier = modifier.fillMaxSize().background(VictimBackground)) {
        Column(
            modifier = Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Skip to demo (top-right, underlined)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.bypassAnonymousEmergency() }.padding(6.dp)
                ) {
                    Text(
                        text = "Skip to demo",
                        fontSize = 14.sp,
                        color = VictimTextMuted,
                        textDecoration = TextDecoration.Underline,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = VictimTextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            NearHelpBrandHeader()
            Spacer(modifier = Modifier.height(20.dp))

            EmergencyButton(
                isLoading = isEmergencyLoading,
                onClick = { viewModel.bypassAnonymousEmergency() },
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Continue with Google (white, bordered, 16dp)
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    try {
                        val intent = AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
                        googleAccountPickerLauncher.launch(intent)
                    } catch (_: Exception) {
                        viewModel.loginWithGoogle("google_oauth_mock_id_token_demo")
                    }
                },
                enabled = !isAuthLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = VictimTextDark,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, VictimBorder),
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(color = VictimPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Continue with Google", fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, color = VictimTextDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = VictimDivider, thickness = 1.dp)
                Text(text = "OR", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VictimTextMuted, modifier = Modifier.padding(horizontal = 14.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = VictimDivider, thickness = 1.dp)
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (uiState is AuthUiState.Error) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, VictimPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Text(text = (uiState as AuthUiState.Error).message, color = Color(0xFFB71C1C), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email label + field
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Email", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = loginForm.email,
                    onValueChange = { viewModel.onLoginEmailChanged(it) },
                    placeholder = { Text("Enter your email") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    isError = loginForm.emailError != null,
                    supportingText = { loginForm.emailError?.let { Text(text = it, color = VictimPrimary, fontSize = 12.sp) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Password", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = loginForm.password,
                    onValueChange = { viewModel.onLoginPasswordChanged(it) },
                    placeholder = { Text("Enter your password") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = VictimTextMuted,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = loginForm.passwordError != null,
                    supportingText = { loginForm.passwordError?.let { Text(text = it, color = VictimPrimary, fontSize = 12.sp) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.login() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.login()
                },
                enabled = !isAuthLoading && loginForm.isValid,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VictimPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = VictimBorder,
                    disabledContentColor = Color(0xFF94A3B8),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            ) {
                if (isEmailLoginLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text(text = "Login", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Don't have an account? ", fontSize = 14.sp, color = VictimTextMuted)
                TextButton(onClick = onNavigateToSignUp, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Text(text = "Sign up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VictimPrimary)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            // Bottom pin + community tagline (simplified skyline)
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VictimPrimary.copy(alpha = 0.85f), modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "SAFER PEOPLE. STRONGER COMMUNITIES.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = VictimTextMuted,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
