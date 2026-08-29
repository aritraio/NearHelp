package com.example.nearhelp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.repository.IAuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

class AuthViewModel(
  private val authRepository: IAuthRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  private val _loginForm = MutableStateFlow(LoginFormData())
  val loginForm: StateFlow<LoginFormData> = _loginForm.asStateFlow()

  private val _signUpForm = MutableStateFlow(SignUpFormData())
  val signUpForm: StateFlow<SignUpFormData> = _signUpForm.asStateFlow()

  private val _otpForm = MutableStateFlow(OtpFormData())
  val otpForm: StateFlow<OtpFormData> = _otpForm.asStateFlow()

  private var timerJob: Job? = null

  // -------------------------------------------------------------
  // Login Form Updates & Validation
  // -------------------------------------------------------------
  fun onLoginEmailChanged(email: String) {
    val error = when {
      email.isBlank() -> "Email cannot be empty"
      !email.trim().matches(EMAIL_REGEX) -> "Invalid email address format"
      else -> null
    }
    _loginForm.update { it.copy(email = email, emailError = error) }
  }

  fun onLoginPasswordChanged(password: String) {
    val error = when {
      password.isBlank() -> "Password cannot be empty"
      password.length < 6 -> "Password must be at least 6 characters"
      else -> null
    }
    _loginForm.update { it.copy(password = password, passwordError = error) }
  }

  fun login() {
    val form = _loginForm.value
    if (!form.isValid) {
      if (form.email.isBlank()) onLoginEmailChanged("")
      if (form.password.isBlank()) onLoginPasswordChanged("")
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Signing into NearHelp...")
      val result = authRepository.login(form.email, form.password)
      result.fold(
        onSuccess = { tokenResponse ->
          _uiState.value = AuthUiState.Success(
            user = tokenResponse.user,
            message = "Welcome back, ${tokenResponse.user.name ?: "User"}!",
          )
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Invalid email or password")
        },
      )
    }
  }

  // -------------------------------------------------------------
  // Sign-Up Form Updates & Validation
  // -------------------------------------------------------------
  fun onSignUpNameChanged(name: String) {
    val error = if (name.isBlank()) "Full name is required" else null
    _signUpForm.update { it.copy(name = name, nameError = error) }
  }

  fun onSignUpEmailChanged(email: String) {
    val error = when {
      email.isBlank() -> "Email cannot be empty"
      !email.trim().matches(EMAIL_REGEX) -> "Invalid email address format"
      else -> null
    }
    _signUpForm.update { it.copy(email = email, emailError = error) }
  }

  fun onSignUpPasswordChanged(password: String) {
    val error = when {
      password.isBlank() -> "Password cannot be empty"
      password.length < 6 -> "Password must be at least 6 characters"
      else -> null
    }
    _signUpForm.update { it.copy(password = password, passwordError = error) }
  }

  fun onSignUpPhoneChanged(phone: String) {
    val cleanPhone = phone.filter { it.isDigit() || it == '+' }
    val error = if (cleanPhone.isNotEmpty() && cleanPhone.length < 8) "Invalid phone number" else null
    _signUpForm.update { it.copy(phone = cleanPhone, phoneError = error) }
  }

  fun onSignUpBloodGroupSelected(bloodGroup: String) {
    _signUpForm.update { it.copy(bloodGroup = bloodGroup) }
  }

  fun onSignUpTermsToggled(accepted: Boolean) {
    _signUpForm.update { it.copy(termsAccepted = accepted) }
  }

  fun signUp() {
    val form = _signUpForm.value
    if (!form.isValid) {
      if (form.name.isBlank()) onSignUpNameChanged("")
      if (form.email.isBlank()) onSignUpEmailChanged("")
      if (form.password.isBlank()) onSignUpPasswordChanged("")
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Creating your NearHelp account...")
      val result = authRepository.register(
        email = form.email,
        password = form.password,
        name = form.name,
        phone = form.phone.takeIf { it.isNotBlank() },
        bloodGroup = form.bloodGroup,
      )
      result.fold(
        onSuccess = { tokenResponse ->
          _uiState.value = AuthUiState.Success(
            user = tokenResponse.user,
            message = "Account created successfully!",
          )
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Registration failed")
        },
      )
    }
  }

  // -------------------------------------------------------------
  // Phone OTP Flow
  // -------------------------------------------------------------
  fun onOtpPhoneNumberChanged(phone: String) {
    val clean = phone.filter { it.isDigit() || it == '+' }
    val error = if (clean.length < 8) "Valid phone number with country code required" else null
    _otpForm.update { it.copy(phoneNumber = clean, phoneError = error) }
  }

  fun onOtpCodeChanged(code: String) {
    val clean = code.filter { it.isDigit() }.take(6)
    _otpForm.update { it.copy(otpCode = clean, otpError = null) }
    if (clean.length == 6) {
      verifyOtp()
    }
  }

  fun sendOtp(phoneNumber: String? = null) {
    val targetPhone = phoneNumber ?: _otpForm.value.phoneNumber
    if (targetPhone.isBlank() || targetPhone.length < 8) {
      _otpForm.update { it.copy(phoneError = "Please enter a valid phone number with country code") }
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Sending OTP code to $targetPhone...")
      val result = authRepository.sendPhoneOtp(targetPhone)
      result.fold(
        onSuccess = { msg ->
          _otpForm.update { it.copy(phoneNumber = targetPhone, otpCode = "") }
          _uiState.value = AuthUiState.OtpSent(targetPhone, msg.message)
          startResendCountdown()
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to send OTP")
        },
      )
    }
  }

  fun verifyOtp() {
    val form = _otpForm.value
    if (form.otpCode.length < 4) {
      _otpForm.update { it.copy(otpError = "Please enter the 6-digit code") }
      return
    }

    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Verifying OTP code...")
      val result = authRepository.verifyPhoneOtp(form.phoneNumber, form.otpCode)
      result.fold(
        onSuccess = { tokenResponse ->
          _uiState.value = AuthUiState.Success(
            user = tokenResponse.user,
            message = "Phone verified successfully!",
          )
        },
        onFailure = { error ->
          _otpForm.update { it.copy(otpError = error.localizedMessage ?: "Invalid OTP code") }
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Invalid or expired OTP code")
        },
      )
    }
  }

  private fun startResendCountdown() {
    timerJob?.cancel()
    _otpForm.update { it.copy(resendCooldownSeconds = 60, canResend = false) }
    timerJob = viewModelScope.launch {
      for (second in 59 downTo 0) {
        delay(1000)
        _otpForm.update { it.copy(resendCooldownSeconds = second, canResend = second == 0) }
      }
    }
  }

  // -------------------------------------------------------------
  // Google Sign-In Flow (Firebase ID Token)
  // -------------------------------------------------------------
  fun loginWithGoogle(idToken: String) {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Signing in with Google...")
      val result = authRepository.loginWithGoogle(idToken)
      result.fold(
        onSuccess = { tokenResponse ->
          _uiState.value = AuthUiState.Success(
            user = tokenResponse.user,
            message = "Signed in with Google!",
          )
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Google sign-in failed")
        },
      )
    }
  }

  // -------------------------------------------------------------
  // Zero-Barrier Anonymous Emergency Mode Bypass
  // -------------------------------------------------------------
  fun bypassAnonymousEmergency(tempName: String = "Anonymous Victim") {
    viewModelScope.launch {
      _uiState.value = AuthUiState.Loading("Activating Emergency Mode...")
      val result = authRepository.createAnonymousEmergencySession(tempName)
      result.fold(
        onSuccess = { tokenResponse ->
          _uiState.value = AuthUiState.Success(
            user = tokenResponse.user,
            message = "Emergency mode active. 1-Tap SOS ready.",
          )
        },
        onFailure = { error ->
          _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Emergency bypass failed")
        },
      )
    }
  }

  fun checkExistingSession(): Boolean = authRepository.isLoggedIn()

  fun getStoredUserName(): String? = authRepository.getStoredUserName()

  fun getStoredUserEmail(): String? = authRepository.getStoredUserEmail()

  fun isAnonymous(): Boolean = authRepository.isAnonymous()

  fun resetUiState() {
    _uiState.value = AuthUiState.Idle
  }

  fun logout() {
    authRepository.logout()
    _uiState.value = AuthUiState.Idle
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}
