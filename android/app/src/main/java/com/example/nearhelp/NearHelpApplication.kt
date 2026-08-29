package com.example.nearhelp

import android.app.Application
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.local.TokenStorage
import com.example.nearhelp.data.repository.AuthRepository
import com.example.nearhelp.data.repository.IAuthRepository

class NearHelpApplication : Application() {

  lateinit var tokenStorage: TokenStorage
    private set

  lateinit var authRepository: IAuthRepository
    private set

  override fun onCreate() {
    super.onCreate()
    instance = this

    tokenStorage = TokenStorage(this)
    val apiService = RetrofitClient.getAuthApiService()
    authRepository = AuthRepository(apiService, tokenStorage)
  }

  companion object {
    lateinit var instance: NearHelpApplication
      private set
  }
}
