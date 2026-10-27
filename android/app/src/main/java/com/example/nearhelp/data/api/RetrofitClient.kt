package com.example.nearhelp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {


  const val PRODUCTION_BASE_URL: String = "https://nearhelp-backend-7sfj.onrender.com/"

  // All devices use one explicit backend; never replay credentials or SOS writes on unrelated LAN hosts.
  val CANDIDATE_BASE_URLS: List<String> = listOf(com.example.nearhelp.BuildConfig.API_BASE_URL)

  @Volatile
  private var activeBaseUrl: String = CANDIDATE_BASE_URLS.first()

  private var authApiService: AuthApiService? = null
  private var userApiService: UserApiService? = null
  private var routingApiService: RoutingApiService? = null
  private var aiAgentApiService: AiAgentApiService? = null

  private val loggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.NONE
    }
  }

  private val okHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(loggingInterceptor)
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  fun getSosApiService(): SosApiService = getRetrofit().create(SosApiService::class.java)

  fun getEffectiveBaseUrl(): String {
    return activeBaseUrl
  }

  private fun getRetrofit(customBaseUrl: String? = null): Retrofit {
    val targetUrl = customBaseUrl ?: getEffectiveBaseUrl()
    return Retrofit.Builder()
      .baseUrl(targetUrl)
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  fun getAuthApiService(customBaseUrl: String? = null): AuthApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (authApiService == null || customBaseUrl != null) {
      authApiService = retrofit.create(AuthApiService::class.java)
    }
    return authApiService!!
  }

  fun getUserApiService(customBaseUrl: String? = null): UserApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (userApiService == null || customBaseUrl != null) {
      userApiService = retrofit.create(UserApiService::class.java)
    }
    return userApiService!!
  }

  fun getRoutingApiService(customBaseUrl: String? = null): RoutingApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (routingApiService == null || customBaseUrl != null) {
      routingApiService = retrofit.create(RoutingApiService::class.java)
    }
    return routingApiService!!
  }

  fun getAiAgentApiService(customBaseUrl: String? = null): AiAgentApiService {
    val retrofit = getRetrofit(customBaseUrl)
    if (aiAgentApiService == null || customBaseUrl != null) {
      aiAgentApiService = retrofit.create(AiAgentApiService::class.java)
    }
    return aiAgentApiService!!
  }

  fun setBaseUrl(newBaseUrl: String) {
    activeBaseUrl = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
    authApiService = null
    userApiService = null
    routingApiService = null
    aiAgentApiService = null
  }
}
