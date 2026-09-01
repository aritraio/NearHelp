package com.example.nearhelp

import android.app.Application
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.repository.AuthRepository
import com.example.nearhelp.data.repository.IAuthRepository

import com.example.nearhelp.data.repository.IUserRepository
import com.example.nearhelp.data.repository.UserRepository
import com.example.nearhelp.data.repository.IRoutingRepository
import com.example.nearhelp.data.repository.RoutingRepository

class NearHelpApplication : Application() {

  lateinit var tokenStorage: TokenStorage
    private set

  lateinit var authRepository: IAuthRepository
    private set

  lateinit var userRepository: IUserRepository
    private set

  lateinit var routingRepository: IRoutingRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this

    tokenStorage = TokenStorage(this)
    val authApiService = RetrofitClient.getAuthApiService()
    val userApiService = RetrofitClient.getUserApiService()
    val routingApiService = RetrofitClient.getRoutingApiService()
    authRepository = AuthRepository(authApiService, tokenStorage)
    userRepository = UserRepository(userApiService, tokenStorage)
    routingRepository = RoutingRepository(routingApiService)
  }

  companion object {
    lateinit var instance: NearHelpApplication
      private set
  }
}
