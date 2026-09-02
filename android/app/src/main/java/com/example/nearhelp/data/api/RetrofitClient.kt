package com.example.nearhelp.data.api

import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

  private fun isRunningOnEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic")
        || Build.FINGERPRINT.startsWith("unknown")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk" == Build.PRODUCT)
  }

  const val PRODUCTION_BASE_URL: String = "https://nearhelp-backend-7sfj.onrender.com/"

  // Production cloud backend (Render + Supabase PostGIS)
  private val DEFAULT_BASE_URL: String
    get() = PRODUCTION_BASE_URL

  private var baseUrl: String? = null
  private var authApiService: AuthApiService? = null
  private var userApiService: UserApiService? = null
  private var routingApiService: RoutingApiService? = null
  private var aiAgentApiService: AiAgentApiService? = null

  private val loggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
  }

  private val okHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(loggingInterceptor)
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  fun getEffectiveBaseUrl(): String {
    return baseUrl ?: DEFAULT_BASE_URL
  }

  private fun getRetrofit(customBaseUrl: String? = null): Retrofit {
    val targetUrl = customBaseUrl ?: getEffectiveBaseUrl()
    if (baseUrl != targetUrl) {
      baseUrl = targetUrl
      authApiService = null
      userApiService = null
      routingApiService = null
      aiAgentApiService = null
    }
    return Retrofit.Builder()
      .baseUrl(getEffectiveBaseUrl())
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  fun getAuthApiService(customBaseUrl: String? = null): AuthApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (authApiService == null) {
      authApiService = retrofit.create(AuthApiService::class.java)
    }
    return authApiService!!
  }

  fun getUserApiService(customBaseUrl: String? = null): UserApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (userApiService == null) {
      userApiService = retrofit.create(UserApiService::class.java)
    }
    return userApiService!!
  }

  fun getRoutingApiService(customBaseUrl: String? = null): RoutingApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (routingApiService == null) {
      routingApiService = retrofit.create(RoutingApiService::class.java)
    }
    return routingApiService!!
  }

  fun getAiAgentApiService(customBaseUrl: String? = null): AiAgentApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (aiAgentApiService == null) {
      aiAgentApiService = retrofit.create(AiAgentApiService::class.java)
    }
    return aiAgentApiService!!
  }

  fun setBaseUrl(newBaseUrl: String) {
    baseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
    authApiService = null
    userApiService = null
    routingApiService = null
    aiAgentApiService = null
  }
}

