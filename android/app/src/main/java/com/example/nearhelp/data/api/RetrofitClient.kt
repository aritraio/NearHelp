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
  private var apiService: AuthApiService? = null

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

  fun getAuthApiService(customBaseUrl: String? = null): AuthApiService {
    val targetUrl = customBaseUrl ?: baseUrl
    if (apiService == null || baseUrl != targetUrl) {
      baseUrl = targetUrl
      val retrofit =
        Retrofit.Builder()
          .baseUrl(baseUrl)
          .client(okHttpClient)
          .addConverterFactory(GsonConverterFactory.create())
          .build()

      apiService = retrofit.create(AuthApiService::class.java)
    }
    return apiService!!
  }

  fun setBaseUrl(newBaseUrl: String) {
    baseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
    apiService = null
  }
}
