package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.AuthApiService
import com.example.nearhelp.data.api.models.AnonymousAuthRequest
import com.example.nearhelp.data.api.models.GoogleAuthRequest
import com.example.nearhelp.data.api.models.LoginRequest
import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.PhoneSendOtpRequest
import com.example.nearhelp.data.api.models.PhoneVerifyRequest
import com.example.nearhelp.data.api.models.RegisterRequest
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.local.ITokenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
  private val apiService: AuthApiService,
  private val tokenStorage: ITokenStorage,
) : IAuthRepository {

  override suspend fun register(
    email: String,
    password: String,
    name: String,
    phone: String?,
    bloodGroup: String?,
  ): Result<TokenResponse> = withContext(Dispatchers.IO) {
    try {
      val req = RegisterRequest(
        email = email.trim(),
        password = password,
        name = name.trim(),
        phone = phone?.takeIf { it.isNotBlank() },
        bloodGroup = bloodGroup?.takeIf { it.isNotBlank() },
      )
      val response = apiService.register(req)
      if (response.isSuccessful && response.body() != null) {
        val tokenResponse = response.body()!!
        tokenStorage.saveSession(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          user = tokenResponse.user,
        )
        Result.success(tokenResponse)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Registration failed (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun login(
    email: String,
    password: String,
  ): Result<TokenResponse> = withContext(Dispatchers.IO) {
    try {
      val req = LoginRequest(
        email = email.trim(),
        password = password,
      )
      val response = apiService.login(req)
      if (response.isSuccessful && response.body() != null) {
        val tokenResponse = response.body()!!
        tokenStorage.saveSession(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          user = tokenResponse.user,
        )
        Result.success(tokenResponse)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Login failed (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun loginWithGoogle(
    idToken: String,
  ): Result<TokenResponse> = withContext(Dispatchers.IO) {
    try {
      val req = GoogleAuthRequest(idToken = idToken)
      val response = apiService.googleAuth(req)
      if (response.isSuccessful && response.body() != null) {
        val tokenResponse = response.body()!!
        tokenStorage.saveSession(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          user = tokenResponse.user,
        )
        Result.success(tokenResponse)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Google sign-in failed (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun sendPhoneOtp(
    phoneNumber: String,
  ): Result<MessageResponse> = withContext(Dispatchers.IO) {
    try {
      val req = PhoneSendOtpRequest(phoneNumber = phoneNumber.trim())
      val response = apiService.sendPhoneOtp(req)
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Failed to send OTP (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun verifyPhoneOtp(
    phoneNumber: String,
    otpCode: String,
    name: String?,
  ): Result<TokenResponse> = withContext(Dispatchers.IO) {
    try {
      val req = PhoneVerifyRequest(
        phoneNumber = phoneNumber.trim(),
        otpCode = otpCode.trim(),
        name = name?.takeIf { it.isNotBlank() },
      )
      val response = apiService.verifyPhoneOtp(req)
      if (response.isSuccessful && response.body() != null) {
        val tokenResponse = response.body()!!
        tokenStorage.saveSession(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          user = tokenResponse.user,
        )
        Result.success(tokenResponse)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "OTP verification failed (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun createAnonymousEmergencySession(
    tempName: String?,
  ): Result<TokenResponse> = withContext(Dispatchers.IO) {
    try {
      val req = AnonymousAuthRequest(
        tempName = tempName ?: "Anonymous Victim",
      )
      val response = apiService.createAnonymousSession(req)
      if (response.isSuccessful && response.body() != null) {
        val tokenResponse = response.body()!!
        tokenStorage.saveSession(
          accessToken = tokenResponse.accessToken,
          refreshToken = tokenResponse.refreshToken,
          user = tokenResponse.user,
        )
        Result.success(tokenResponse)
      } else {
        val errorMsg = response.errorBody()?.string() ?: "Emergency bypass failed (${response.code()})"
        Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun fetchCurrentUser(): Result<UserResponse> = withContext(Dispatchers.IO) {
    try {
      val token = tokenStorage.getAccessToken()
      if (token.isNullOrBlank()) {
        return@withContext Result.failure(Exception("No active session found"))
      }
      val response = apiService.getCurrentUser("Bearer $token")
      if (response.isSuccessful && response.body() != null) {
        Result.success(response.body()!!)
      } else {
        Result.failure(Exception("Failed to fetch user (${response.code()})"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun isLoggedIn(): Boolean = tokenStorage.isLoggedIn()

  override fun isAnonymous(): Boolean = tokenStorage.isAnonymous()

  override fun getStoredUserId(): String? = tokenStorage.getUserId()

  override fun getStoredUserName(): String? = tokenStorage.getUserName()

  override fun getStoredUserEmail(): String? = tokenStorage.getUserEmail()

  override fun getStoredBloodGroup(): String? = tokenStorage.getBloodGroup()

  override fun getStoredAccessToken(): String? = tokenStorage.getAccessToken()

  override fun logout() {
    tokenStorage.clear()
  }
}
