package com.example.nearhelp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

  // Android emulator points to host machine via 10.0.2.2
  private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

  private var baseUrl: String = DEFAULT_BASE_URL
  private var authApiService: AuthApiService? = null
  private var userApiService: UserApiService? = null

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

  private fun getRetrofit(customBaseUrl: String? = null): Retrofit {
    val targetUrl = customBaseUrl ?: baseUrl
    if (baseUrl != targetUrl) {
      baseUrl = targetUrl
      authApiService = null
      userApiService = null
    }
    return Retrofit.Builder()
      .baseUrl(baseUrl)
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

  fun setBaseUrl(newBaseUrl: String) {
    baseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
    authApiService = null
    userApiService = null
  }
}
