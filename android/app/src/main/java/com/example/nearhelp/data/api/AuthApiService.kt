package com.example.nearhelp.data.api

import com.example.nearhelp.data.api.models.AnonymousAuthRequest
import com.example.nearhelp.data.api.models.DeviceRegisterRequest
import com.example.nearhelp.data.api.models.GoogleAuthRequest
import com.example.nearhelp.data.api.models.LoginRequest
import com.example.nearhelp.data.api.models.MessageResponse
import com.example.nearhelp.data.api.models.PhoneSendOtpRequest
import com.example.nearhelp.data.api.models.PhoneVerifyRequest
import com.example.nearhelp.data.api.models.RegisterRequest
import com.example.nearhelp.data.api.models.TokenRefreshRequest
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

  @POST("api/v1/auth/register")
  suspend fun register(
    @Body request: RegisterRequest
  ): Response<TokenResponse>

  @POST("api/v1/auth/login")
  suspend fun login(
    @Body request: LoginRequest
  ): Response<TokenResponse>

  @POST("api/v1/auth/google")
  suspend fun googleAuth(
    @Body request: GoogleAuthRequest
  ): Response<TokenResponse>

  @POST("api/v1/auth/phone/send-otp")
  suspend fun sendPhoneOtp(
    @Body request: PhoneSendOtpRequest
  ): Response<MessageResponse>

  @POST("api/v1/auth/phone/verify")
  suspend fun verifyPhoneOtp(
    @Body request: PhoneVerifyRequest
  ): Response<TokenResponse>

  @POST("api/v1/auth/anonymous")
  suspend fun createAnonymousSession(
    @Body request: AnonymousAuthRequest = AnonymousAuthRequest()
  ): Response<TokenResponse>

  @POST("api/v1/auth/refresh")
  suspend fun refreshToken(
    @Body request: TokenRefreshRequest
  ): Response<TokenResponse>

  @POST("api/v1/auth/device")
  suspend fun registerDevice(
    @Header("Authorization") bearerToken: String,
    @Body request: DeviceRegisterRequest
  ): Response<MessageResponse>

  @GET("api/v1/auth/me")
  suspend fun getCurrentUser(
    @Header("Authorization") bearerToken: String
  ): Response<UserResponse>
}
