package com.example.nearhelp.ui.auth

import com.example.nearhelp.data.api.models.UserResponse

sealed interface AuthUiState {
  data object Idle : AuthUiState
  data class Loading(val message: String = "Please wait...") : AuthUiState
  data class Success(val user: UserResponse, val message: String) : AuthUiState
  data class OtpSent(val phoneNumber: String, val message: String) : AuthUiState
  data class Error(val message: String) : AuthUiState
}

data class LoginFormData(
  val email: String = "",
  val emailError: String? = null,
  val password: String = "",
  val passwordError: String? = null,
) {
  val isValid: Boolean
    get() = email.isNotBlank() && emailError == null && password.isNotBlank() && passwordError == null
}

data class SignUpFormData(
  val name: String = "",
  val nameError: String? = null,
  val email: String = "",
  val emailError: String? = null,
  val password: String = "",
  val passwordError: String? = null,
  val phone: String = "",
  val phoneError: String? = null,
  val bloodGroup: String? = null,
  val termsAccepted: Boolean = true,
) {
  val isValid: Boolean
    get() = name.isNotBlank() && nameError == null &&
      email.isNotBlank() && emailError == null &&
      password.isNotBlank() && passwordError == null &&
      termsAccepted
}

data class OtpFormData(
  val phoneNumber: String = "",
  val phoneError: String? = null,
  val otpCode: String = "",
  val otpError: String? = null,
  val resendCooldownSeconds: Int = 60,
  val canResend: Boolean = false,
)
