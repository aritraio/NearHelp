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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.theme.CrisisSurfaceBg
import com.example.nearhelp.theme.EmergencyCrimson
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.components.OtpInputField

@Composable
fun PhoneOtpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val otpForm by viewModel.otpForm.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var isOtpSent by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.OtpSent) {
            isOtpSent = true
        }
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
            // Top Bar with Back
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
                    text = "Phone Verification",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0F172A),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Circle
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isOtpSent) Icons.Default.Sms else Icons.Default.Phone,
                    contentDescription = "Phone verification",
                    tint = EmergencyCrimson,
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isOtpSent) "Enter Verification Code" else "Phone OTP Sign-In",
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOtpSent) {
                    "We sent a 6-digit verification code to ${otpForm.phoneNumber}"
                } else {
                    "Enter your mobile number with country code to receive an instant verification code"
                },
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                ),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Alert Box
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

            if (!isOtpSent) {
                // Step 1: Input Phone Number
                OutlinedTextField(
                    value = otpForm.phoneNumber,
                    onValueChange = { viewModel.onOtpPhoneNumberChanged(it) },
                    label = { Text("Mobile Number (with country code)", fontFamily = FontFamily.SansSerif) },
                    placeholder = { Text("+919876543210", fontFamily = FontFamily.SansSerif) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    isError = otpForm.phoneError != null,
                    supportingText = {
                        if (otpForm.phoneError != null) {
                            Text(
                                text = otpForm.phoneError!!,
                                color = EmergencyCrimson,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                            )
                        } else {
                            Text(
                                text = "e.g. +91 98765 43210 for India",
                                color = Color(0xFF64748B),
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

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendOtp()
                    },
                    enabled = !isLoading && otpForm.phoneNumber.length >= 8,
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
                            text = "Send Verification OTP",
                            fontFamily = FontFamily.SansSerif,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            ),
                        )
                    }
                }
            } else {
                // Step 2: Input 6-Digit OTP
                OtpInputField(
                    otpValue = otpForm.otpCode,
                    onOtpChange = { viewModel.onOtpCodeChanged(it) },
                    isError = otpForm.otpError != null,
                )

                if (otpForm.otpError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = otpForm.otpError!!,
                        color = EmergencyCrimson,
                        fontFamily = FontFamily.SansSerif,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resend Timer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { isOtpSent = false }) {
                        Text(
                            text = "Change Number",
                            fontFamily = FontFamily.SansSerif,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                        )
                    }

                    if (otpForm.canResend) {
                        TextButton(onClick = { viewModel.sendOtp() }) {
                            Text(
                                text = "Resend Code",
                                fontFamily = FontFamily.SansSerif,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyCrimson,
                                ),
                            )
                        }
                    } else {
                        Text(
                            text = "Resend in ${otpForm.resendCooldownSeconds}s",
                            fontFamily = FontFamily.SansSerif,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Verify Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.verifyOtp()
                    },
                    enabled = !isLoading && otpForm.otpCode.length >= 4,
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
                            text = "Verify & Proceed",
                            fontFamily = FontFamily.SansSerif,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            ),
                        )
                    }
                }
            }
        }
    }
}


