package com.example.nearhelp.data.api

import android.os.Build
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
  private const val TAG = "RetrofitClient"

  const val PRODUCTION_BASE_URL: String = "https://nearhelp-backend-7sfj.onrender.com/"

  // Host PC Wi-Fi LAN IP address candidates
  const val DEV_HOST_LAN_IP_1 = "192.168.0.106"
  const val DEV_HOST_LAN_IP_2 = "192.168.29.178"

  fun isRunningOnEmulator(): Boolean {
    return (Build.FINGERPRINT.startsWith("generic")
        || Build.MODEL.contains("google_sdk")
        || Build.MODEL.contains("Emulator")
        || Build.MODEL.contains("Android SDK built for x86")
        || Build.HARDWARE.contains("goldfish")
        || Build.HARDWARE.contains("ranchu")
        || Build.MANUFACTURER.contains("Genymotion")
        || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        || "google_sdk" == Build.PRODUCT
        || Build.PRODUCT.contains("sdk_gphone")
        || Build.PRODUCT.contains("vbox86p"))
  }

  // Ordered candidate base URLs to try (production cloud + local dev environments)
  val CANDIDATE_BASE_URLS: List<String> = if (isRunningOnEmulator()) {
    listOf(
      "http://10.0.2.2:8000/",
      "http://127.0.0.1:8000/",
      PRODUCTION_BASE_URL,
      "http://$DEV_HOST_LAN_IP_1:8000/",
      "http://$DEV_HOST_LAN_IP_2:8000/"
    )
  } else {
    listOf(
      "http://127.0.0.1:8000/",
      PRODUCTION_BASE_URL,
      "http://$DEV_HOST_LAN_IP_1:8000/",
      "http://$DEV_HOST_LAN_IP_2:8000/",
      "http://10.0.2.2:8000/"
    )
  }

  @Volatile
  private var activeBaseUrl: String = CANDIDATE_BASE_URLS.first()

  private var authApiService: AuthApiService? = null
  private var userApiService: UserApiService? = null
  private var routingApiService: RoutingApiService? = null
  private var aiAgentApiService: AiAgentApiService? = null

  private val hostFailoverInterceptor = Interceptor { chain ->
    val originalRequest = chain.request()
    val originalHttpUrl = originalRequest.url

    // Try current target host first
    var lastException: IOException? = null
    try {
      return@Interceptor chain.proceed(originalRequest)
    } catch (e: IOException) {
      lastException = e
      Log.w(TAG, "Request to ${originalHttpUrl.host}:${originalHttpUrl.port} failed (${e.message}). Attempting failover candidates...")
    }

    // Try remaining candidate hosts
    for (candidate in CANDIDATE_BASE_URLS) {
      val candidateHttpUrl = candidate.toHttpUrlOrNull() ?: continue
      if (candidateHttpUrl.host == originalHttpUrl.host && candidateHttpUrl.port == originalHttpUrl.port) {
        continue // Already tried
      }

      val newUrl = originalHttpUrl.newBuilder()
        .scheme(candidateHttpUrl.scheme)
        .host(candidateHttpUrl.host)
        .port(candidateHttpUrl.port)
        .build()

      val newRequest = originalRequest.newBuilder()
        .url(newUrl)
        .build()

      try {
        val response = chain.proceed(newRequest)
        // If reached server successfully, save this candidate as active baseUrl
        activeBaseUrl = candidate
        Log.i(TAG, "Successfully connected to candidate host: $candidate. Setting as active base URL.")
        return@Interceptor response
      } catch (e: IOException) {
        lastException = e
        Log.w(TAG, "Candidate host $candidate failed: ${e.message}")
      }
    }

    throw lastException ?: IOException("Failed to connect to any backend host candidates: $CANDIDATE_BASE_URLS")
  }

  private val loggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }
  }

  private val okHttpClient by lazy {
    OkHttpClient.Builder()
      .addInterceptor(hostFailoverInterceptor)
      .addInterceptor(loggingInterceptor)
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .writeTimeout(15, TimeUnit.SECONDS)
      .build()
  }

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
