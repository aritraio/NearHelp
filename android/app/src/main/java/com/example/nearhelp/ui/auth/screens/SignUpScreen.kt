package com.example.nearhelp.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.theme.CrisisSurfaceBg
import com.example.nearhelp.theme.EmergencyCrimson
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.components.AuthHeader
import com.example.nearhelp.ui.auth.components.BloodGroupSelector

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val signUpForm by viewModel.signUpForm.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onNavigateToHome()
            viewModel.resetUiState()
        }
    }

    val isLoading = uiState is AuthUiState.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CrisisSurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Back navigation icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = Color(0xFF0F172A),
                    )
                }
                Text(
                    text = "Create Profile",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Header
            AuthHeader(
                title = "Join NearHelp",
                subtitle = "Register to trigger SOS alerts, receive community rescues, and manage your Medical ID",
                showLogo = false,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Box
            if (uiState is AuthUiState.Error) {
                val errorMsg = (uiState as AuthUiState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, EmergencyCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFB71C1C),
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Name Input
            OutlinedTextField(
                value = signUpForm.name,
                onValueChange = { viewModel.onSignUpNameChanged(it) },
                label = { Text("Full Name *", fontFamily = FontFamily.SansSerif) },
                placeholder = { Text("e.g. John Doe", fontFamily = FontFamily.SansSerif) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name Icon",
                        tint = if (signUpForm.nameError != null) EmergencyCrimson else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp),
                    )
                },
                isError = signUpForm.nameError != null,
                supportingText = {
                    if (signUpForm.nameError != null) {
                        Text(
                            text = signUpForm.nameError!!,
                            color = EmergencyCrimson,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyCrimson,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = EmergencyCrimson,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedLabelColor = EmergencyCrimson,
                    unfocusedLabelColor = Color(0xFF64748B),
                    focusedPlaceholderColor = Color(0xFF94A3B8),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Input
            OutlinedTextField(
                value = signUpForm.email,
                onValueChange = { viewModel.onSignUpEmailChanged(it) },
                label = { Text("Email Address *", fontFamily = FontFamily.SansSerif) },
                placeholder = { Text("name@example.com", fontFamily = FontFamily.SansSerif) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = if (signUpForm.emailError != null) EmergencyCrimson else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp),
                    )
                },
                isError = signUpForm.emailError != null,
                supportingText = {
                    if (signUpForm.emailError != null) {
                        Text(
                            text = signUpForm.emailError!!,
                            color = EmergencyCrimson,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyCrimson,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = EmergencyCrimson,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedLabelColor = EmergencyCrimson,
                    unfocusedLabelColor = Color(0xFF64748B),
                    focusedPlaceholderColor = Color(0xFF94A3B8),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Input
            OutlinedTextField(
                value = signUpForm.password,
                onValueChange = { viewModel.onSignUpPasswordChanged(it) },
                label = { Text("Password (min 6 characters) *", fontFamily = FontFamily.SansSerif) },
                placeholder = { Text("••••••••", fontFamily = FontFamily.SansSerif) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = if (signUpForm.passwordError != null) EmergencyCrimson else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = signUpForm.passwordError != null,
                supportingText = {
                    if (signUpForm.passwordError != null) {
                        Text(
                            text = signUpForm.passwordError!!,
                            color = EmergencyCrimson,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyCrimson,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = EmergencyCrimson,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedLabelColor = EmergencyCrimson,
                    unfocusedLabelColor = Color(0xFF64748B),
                    focusedPlaceholderColor = Color(0xFF94A3B8),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Input (Optional)
            OutlinedTextField(
                value = signUpForm.phone,
                onValueChange = { viewModel.onSignUpPhoneChanged(it) },
                label = { Text("Phone Number (Optional for SMS / OTP)", fontFamily = FontFamily.SansSerif) },
                placeholder = { Text("+919876543210", fontFamily = FontFamily.SansSerif) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone Icon",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp),
                    )
                },
                isError = signUpForm.phoneError != null,
                supportingText = {
                    if (signUpForm.phoneError != null) {
                        Text(
                            text = signUpForm.phoneError!!,
                            color = EmergencyCrimson,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmergencyCrimson,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = EmergencyCrimson,
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedLabelColor = EmergencyCrimson,
                    unfocusedLabelColor = Color(0xFF64748B),
                    focusedPlaceholderColor = Color(0xFF94A3B8),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Blood Group Selection
            BloodGroupSelector(
                selectedBloodGroup = signUpForm.bloodGroup,
                onBloodGroupSelected = { viewModel.onSignUpBloodGroupSelected(it) },
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Good Samaritan Consent Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = signUpForm.termsAccepted,
                    onCheckedChange = { viewModel.onSignUpTermsToggled(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = EmergencyCrimson,
                        uncheckedColor = Color(0xFF64748B),
                    ),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I agree to NearHelp Emergency Response terms and Good Samaritan protocols (India 2016).",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = Color(0xFF475569),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sign Up Action Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.signUp()
                },
                enabled = !isLoading && signUpForm.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmergencyCrimson,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8),
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link back to Login
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Already have an account?",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFF64748B),
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmergencyCrimson,
                        ),
                    )
                }
            }
        }
    }
}


