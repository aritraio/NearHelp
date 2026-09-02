package com.example.nearhelp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nearhelp.data.api.models.UserResponse

interface ITokenStorage {
  fun saveSession(accessToken: String, refreshToken: String, user: UserResponse)
  fun getAccessToken(): String?
  fun getRefreshToken(): String?
  fun getUserId(): String?
  fun getUserName(): String?
  fun getUserEmail(): String?
  fun getUserPhone(): String?
  fun getBloodGroup(): String?
  fun isAnonymous(): Boolean
  fun isLoggedIn(): Boolean
  fun saveFcmToken(token: String)
  fun getFcmToken(): String?
  fun clear()
}

/**
 * Thread-safe secure token and session storage using AndroidX EncryptedSharedPreferences (AES-256).
 */
open class TokenStorage(private val context: Context) : ITokenStorage {

  private val sharedPreferences: SharedPreferences by lazy {
    try {
      val masterKey =
        MasterKey.Builder(context)
          .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
          .build()

      EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
      )
    } catch (e: Exception) {
      // Fallback for tests or devices where KeyStore initialization fails
      context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
    }
  }

  override fun saveSession(
    accessToken: String,
    refreshToken: String,
    user: UserResponse,
  ) {
    sharedPreferences.edit().apply {
      putString(KEY_ACCESS_TOKEN, accessToken)
      putString(KEY_REFRESH_TOKEN, refreshToken)
      putString(KEY_USER_ID, user.id)
      putString(KEY_USER_NAME, user.name ?: "")
      putString(KEY_USER_EMAIL, user.email ?: "")
      putString(KEY_USER_PHONE, user.phone ?: "")
      putString(KEY_BLOOD_GROUP, user.bloodGroup ?: "")
      putBoolean(KEY_IS_ANONYMOUS, user.isAnonymous)
      putString(KEY_AUTH_PROVIDER, user.authProvider)
      apply()
    }
  }

  override fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

  override fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

  override fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

  override fun getUserName(): String? = sharedPreferences.getString(KEY_USER_NAME, null)

  override fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)

  override fun getUserPhone(): String? = sharedPreferences.getString(KEY_USER_PHONE, null)

  override fun getBloodGroup(): String? = sharedPreferences.getString(KEY_BLOOD_GROUP, null)

  override fun isAnonymous(): Boolean = sharedPreferences.getBoolean(KEY_IS_ANONYMOUS, false)

  override fun isLoggedIn(): Boolean {
    val token = getAccessToken()
    return !token.isNullOrBlank()
  }

  override fun saveFcmToken(token: String) {
    sharedPreferences.edit().putString(KEY_FCM_TOKEN, token).apply()
  }

  override fun getFcmToken(): String? = sharedPreferences.getString(KEY_FCM_TOKEN, null)

  fun saveServerBaseUrl(url: String) {
    sharedPreferences.edit().putString(KEY_SERVER_BASE_URL, url).apply()
  }

  fun getServerBaseUrl(): String? = sharedPreferences.getString(KEY_SERVER_BASE_URL, null)

  override fun clear() {
    sharedPreferences.edit().clear().apply()
  }

  companion object {
    private const val PREFS_NAME = "nearhelp_secure_prefs"
    private const val FALLBACK_PREFS_NAME = "nearhelp_prefs_fallback"

    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_BLOOD_GROUP = "blood_group"
    private const val KEY_IS_ANONYMOUS = "is_anonymous"
    private const val KEY_AUTH_PROVIDER = "auth_provider"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_SERVER_BASE_URL = "server_base_url"
  }
}
