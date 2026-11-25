package com.example.nearhelp

import android.app.Application
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.repository.AiAgentRepository
import com.example.nearhelp.data.repository.AuthRepository
import com.example.nearhelp.data.repository.IAiAgentRepository
import com.example.nearhelp.data.repository.IAuthRepository
import com.example.nearhelp.data.repository.IUserRepository
import com.example.nearhelp.data.repository.UserRepository

class NearHelpApplication : Application() {

  lateinit var tokenStorage: TokenStorage
    private set

  lateinit var authRepository: IAuthRepository
    private set

  lateinit var userRepository: IUserRepository
    private set

  lateinit var aiAgentRepository: IAiAgentRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this
    com.example.nearhelp.theme.ThemeManager.initialize(this)

    tokenStorage = TokenStorage(this)
    val savedServerUrl = tokenStorage.getServerBaseUrl()
    if (!savedServerUrl.isNullOrBlank() &&
        !savedServerUrl.contains("onrender.com") &&
        !savedServerUrl.contains("192.168.") &&
        !savedServerUrl.contains("twin-greatly-secretariat-members") &&
        !savedServerUrl.contains("trycloudflare.com")) {
      RetrofitClient.setBaseUrl(savedServerUrl)
    } else {
      RetrofitClient.setBaseUrl(com.example.nearhelp.BuildConfig.API_BASE_URL)
    }

    val authApiService = RetrofitClient.getAuthApiService()
    val userApiService = RetrofitClient.getUserApiService()
    val aiAgentApiService = RetrofitClient.getAiAgentApiService()
    authRepository = AuthRepository(authApiService, tokenStorage)
    userRepository = UserRepository(userApiService, tokenStorage)
    aiAgentRepository = AiAgentRepository(aiAgentApiService, tokenStorage)
  }

  companion object {
    lateinit var instance: NearHelpApplication
      private set
  }
}
