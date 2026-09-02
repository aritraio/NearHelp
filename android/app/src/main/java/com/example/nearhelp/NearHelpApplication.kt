package com.example.nearhelp

import android.app.Application
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.repository.AiAgentRepository
import com.example.nearhelp.data.repository.AuthRepository
import com.example.nearhelp.data.repository.IAiAgentRepository
import com.example.nearhelp.data.repository.IAuthRepository
import com.example.nearhelp.data.repository.IRoutingRepository
import com.example.nearhelp.data.repository.IUserRepository
import com.example.nearhelp.data.repository.RoutingRepository
import com.example.nearhelp.data.repository.UserRepository

class NearHelpApplication : Application() {

  lateinit var tokenStorage: TokenStorage
    private set

  lateinit var authRepository: IAuthRepository
    private set

  lateinit var userRepository: IUserRepository
    private set

  lateinit var routingRepository: IRoutingRepository
    private set

  lateinit var aiAgentRepository: IAiAgentRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this

    tokenStorage = TokenStorage(this)
    val savedServerUrl = tokenStorage.getServerBaseUrl()
    if (!savedServerUrl.isNullOrBlank()) {
      RetrofitClient.setBaseUrl(savedServerUrl)
    }

    val authApiService = RetrofitClient.getAuthApiService()
    val userApiService = RetrofitClient.getUserApiService()
    val routingApiService = RetrofitClient.getRoutingApiService()
    val aiAgentApiService = RetrofitClient.getAiAgentApiService()
    authRepository = AuthRepository(authApiService, tokenStorage)
    userRepository = UserRepository(userApiService, tokenStorage)
    routingRepository = RoutingRepository(routingApiService)
    aiAgentRepository = AiAgentRepository(aiAgentApiService)
  }

  companion object {
    lateinit var instance: NearHelpApplication
      private set
  }
}
