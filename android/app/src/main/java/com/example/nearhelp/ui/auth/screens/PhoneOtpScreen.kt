package com.example.nearhelp.ui.auth.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted
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

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 24.dp),
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
          tint = TextHighContrast,
        )
      }
      Text(
        text = "Phone Verification",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextHighContrast,
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Icon Circle
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(Color(0xFF222222))
        .border(2.dp, AiCyan, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = if (isOtpSent) Icons.Default.Sms else Icons.Default.Phone,
        contentDescription = "Phone verification",
        tint = AiCyan,
        modifier = Modifier.size(40.dp),
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = if (isOtpSent) "Enter Verification Code" else "Phone OTP Sign-In",
      style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      color = TextHighContrast,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = if (isOtpSent) {
        "We sent a 6-digit verification code to ${otpForm.phoneNumber}"
      } else {
        "Enter your mobile number with country code to receive an instant verification code"
      },
      style = MaterialTheme.typography.bodyMedium,
      color = TextMediumContrast,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Error Alert Box
    if (uiState is AuthUiState.Error) {
      val errorMsg = (uiState as AuthUiState.Error).message
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF3B1212))
          .border(1.dp, EmergencyRed, RoundedCornerShape(10.dp))
          .padding(12.dp),
      ) {
        Text(
          text = errorMsg,
          color = Color(0xFFFF8A80),
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    if (!isOtpSent) {
      // Step 1: Input Phone Number
      OutlinedTextField(
        value = otpForm.phoneNumber,
        onValueChange = { viewModel.onOtpPhoneNumberChanged(it) },
        label = { Text("Mobile Number (with country code)") },
        placeholder = { Text("+919876543210") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Phone",
            tint = AiCyan,
          )
        },
        isError = otpForm.phoneError != null,
        supportingText = {
          if (otpForm.phoneError != null) {
            Text(text = otpForm.phoneError!!, color = EmergencyRed)
          } else {
            Text(text = "e.g. +91 98765 43210 for India", color = TextMuted)
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Phone,
          imeAction = ImeAction.Done,
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AiCyan,
          unfocusedBorderColor = SurfaceBorder,
          focusedContainerColor = CardSurface,
          unfocusedContainerColor = CardSurface,
          focusedTextColor = TextHighContrast,
          unfocusedTextColor = TextHighContrast,
        ),
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = { viewModel.sendOtp() },
        enabled = !isLoading && otpForm.phoneNumber.length >= 8,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = EmergencyRed,
          contentColor = Color.White,
        ),
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
          color = EmergencyRed,
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Resend Timer Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = { isOtpSent = false }) {
          Text(
            text = "Change Number",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMediumContrast),
          )
        }

        if (otpForm.canResend) {
          TextButton(onClick = { viewModel.sendOtp() }) {
            Text(
              text = "Resend Code",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = AiCyan,
              ),
            )
          }
        } else {
          Text(
            text = "Resend in ${otpForm.resendCooldownSeconds}s",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Verify Button
      Button(
        onClick = { viewModel.verifyOtp() },
        enabled = !isLoading && otpForm.otpCode.length >= 4,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = EmergencyRed,
          contentColor = Color.White,
        ),
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
