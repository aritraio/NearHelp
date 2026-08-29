package com.example.nearhelp

import android.app.Application
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.repository.AuthRepository
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

  override fun onCreate() {
    super.onCreate()
    instance = this

    tokenStorage = TokenStorage(this)
    val authApiService = RetrofitClient.getAuthApiService()
    val userApiService = RetrofitClient.getUserApiService()
    authRepository = AuthRepository(authApiService, tokenStorage)
    userRepository = UserRepository(userApiService, tokenStorage)
  }

  companion object {
    lateinit var instance: NearHelpApplication
      private set
  }
}
