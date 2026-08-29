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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted
import com.example.nearhelp.ui.auth.AuthUiState
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.auth.components.AuthHeader
import com.example.nearhelp.ui.auth.components.EmergencyButton

@Composable
fun LoginScreen(
  onNavigateToSignUp: () -> Unit,
  onNavigateToPhoneOtp: () -> Unit,
  onNavigateToHome: () -> Unit,
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val loginForm by viewModel.loginForm.collectAsStateWithLifecycle()

  var passwordVisible by remember { mutableStateOf(false) }

  LaunchedEffect(uiState) {
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
      .padding(horizontal = 24.dp, vertical = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Branding Header
    AuthHeader(
      title = "Welcome to NearHelp",
      subtitle = "Sign in to access emergency response network & verified profile",
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 1-Tap Anonymous Emergency Access Bypass
    EmergencyButton(
      isLoading = isLoading && (uiState as? AuthUiState.Loading)?.message?.contains("Emergency") == true,
      onClick = { viewModel.bypassAnonymousEmergency() },
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Or Divider
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = SurfaceBorder,
      )
      Text(
        text = "OR SIGN IN WITH CREDENTIALS",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
        ),
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 12.dp),
      )
      HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = SurfaceBorder,
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Error Message Card
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

    // Email Input Field
    OutlinedTextField(
      value = loginForm.email,
      onValueChange = { viewModel.onLoginEmailChanged(it) },
      label = { Text("Email Address") },
      placeholder = { Text("name@example.com") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Email,
          contentDescription = "Email Icon",
          tint = if (loginForm.emailError != null) EmergencyRed else TextMediumContrast,
        )
      },
      isError = loginForm.emailError != null,
      supportingText = {
        if (loginForm.emailError != null) {
          Text(text = loginForm.emailError!!, color = EmergencyRed)
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next,
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

    Spacer(modifier = Modifier.height(8.dp))

    // Password Input Field
    OutlinedTextField(
      value = loginForm.password,
      onValueChange = { viewModel.onLoginPasswordChanged(it) },
      label = { Text("Password") },
      placeholder = { Text("••••••••") },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Password Icon",
          tint = if (loginForm.passwordError != null) EmergencyRed else TextMediumContrast,
        )
      },
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (passwordVisible) "Hide password" else "Show password",
            tint = TextMediumContrast,
          )
        }
      },
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      isError = loginForm.passwordError != null,
      supportingText = {
        if (loginForm.passwordError != null) {
          Text(text = loginForm.passwordError!!, color = EmergencyRed)
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(onDone = { viewModel.login() }),
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

    Spacer(modifier = Modifier.height(16.dp))

    // Sign In Button
    Button(
      onClick = { viewModel.login() },
      enabled = !isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = EmergencyRed,
        contentColor = Color.White,
      ),
    ) {
      if (isLoading && (uiState as? AuthUiState.Loading)?.message?.contains("Signing into") == true) {
        CircularProgressIndicator(
          color = Color.White,
          modifier = Modifier.size(22.dp),
          strokeWidth = 2.dp,
        )
      } else {
        Text(
          text = "Sign In",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
          ),
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Phone OTP Button
    OutlinedButton(
      onClick = onNavigateToPhoneOtp,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = TextHighContrast,
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    ) {
      Icon(
        imageVector = Icons.Default.Phone,
        contentDescription = "Phone OTP",
        tint = AiCyan,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Sign in with Phone OTP",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Google Sign-In Button (Firebase OAuth Simulation)
    OutlinedButton(
      onClick = {
        viewModel.loginWithGoogle("google_oauth_mock_id_token_demo_dishari")
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = TextHighContrast,
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    ) {
      Text(
        text = "G",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Black,
          color = Color(0xFF4285F4),
        ),
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Continue with Google",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Don't have an account link
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Text(
        text = "Don't have an account?",
        style = MaterialTheme.typography.bodyMedium,
        color = TextMediumContrast,
      )
      TextButton(onClick = onNavigateToSignUp) {
        Text(
          text = "Sign Up",
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = AiCyan,
          ),
        )
      }
    }
  }
}
