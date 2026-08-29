package com.example.nearhelp.data.repository

import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.models.UserResponse

interface IAuthRepository {
  suspend fun register(
    email: String,
    password: String,
    name: String,
    phone: String? = null,
    bloodGroup: String? = null,
  ): Result<TokenResponse>

  suspend fun login(
    email: String,
    password: String,
  ): Result<TokenResponse>

  suspend fun loginWithGoogle(
    idToken: String,
  ): Result<TokenResponse>

  suspend fun sendPhoneOtp(
    phoneNumber: String,
  ): Result<MessageResponse>

  suspend fun verifyPhoneOtp(
    phoneNumber: String,
    otpCode: String,
    name: String? = null,
  ): Result<TokenResponse>

  suspend fun createAnonymousEmergencySession(
    tempName: String? = "Anonymous Victim",
  ): Result<TokenResponse>

  suspend fun fetchCurrentUser(): Result<UserResponse>

  fun isLoggedIn(): Boolean
  fun isAnonymous(): Boolean
  fun getStoredUserId(): String?
  fun getStoredUserName(): String?
  fun getStoredUserEmail(): String?
  fun getStoredBloodGroup(): String?
  fun getStoredAccessToken(): String?
  fun logout()
}
