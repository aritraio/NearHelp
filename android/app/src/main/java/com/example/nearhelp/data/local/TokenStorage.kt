package com.example.nearhelp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nearhelp.data.api.models.UserResponse
import com.example.nearhelp.data.api.models.TokenResponse
import com.example.nearhelp.data.api.Incident
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class RecordedIncident(val id: String, val address: String, val status: String, val timestamp: String, val receivedHelp: Boolean)

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
  fun saveCachedUserProfile(user: UserResponse) {}
  fun getCachedUserProfile(): UserResponse? = null
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

  @Synchronized
  override fun saveSession(
    accessToken: String,
    refreshToken: String,
    user: UserResponse,
  ) {
    saveCachedUserProfile(user)
    sharedPreferences.edit().apply {
      if (getUserId() != user.id) {
        remove("active_incident_id")
        remove("pending_sos_key")
        remove("pending_sos_payload")
        remove("pending_sos_cancel")
      }
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

  override fun saveCachedUserProfile(user: UserResponse) {
    try {
      sharedPreferences.edit().putString(KEY_CACHED_USER_PROFILE, Gson().toJson(user)).apply()
    } catch (_: Exception) {}
  }

  override fun getCachedUserProfile(): UserResponse? {
    val json = sharedPreferences.getString(KEY_CACHED_USER_PROFILE, null) ?: return null
    return try {
      Gson().fromJson(json, UserResponse::class.java)
    } catch (_: Exception) {
      null
    }
  }

  override fun getAccessToken(): String? = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)

  override fun getRefreshToken(): String? = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)

  /** An in-flight refresh cannot restore a session after logout or an account switch. */
  @Synchronized
  fun saveRefreshedSession(expectedRefresh: String, expectedUserId: String?, session: TokenResponse): Boolean {
    if (getRefreshToken() != expectedRefresh || getUserId() != expectedUserId || session.user.id != expectedUserId) return false
    saveSession(session.accessToken, session.refreshToken, session.user)
    return true
  }

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

  fun savePendingSos(key: String?, payload: String?) {
    sharedPreferences.edit().putString("pending_sos_key", key).putString("pending_sos_payload", payload).apply()
  }
  fun getPendingSosKey(): String? = sharedPreferences.getString("pending_sos_key", null)
  fun getPendingSosPayload(): String? = sharedPreferences.getString("pending_sos_payload", null)
  fun savePendingSosCancellation(value: Boolean) { sharedPreferences.edit().putBoolean("pending_sos_cancel", value).apply() }
  fun getPendingSosCancellation(): Boolean = sharedPreferences.getBoolean("pending_sos_cancel", false)

  fun saveActiveIncidentId(id: String?) {
    sharedPreferences.edit().putString("active_incident_id", id).apply()
  }

  fun getActiveIncidentId(): String? = sharedPreferences.getString("active_incident_id", null)

  fun getIncidentHistory(): List<RecordedIncident> = runCatching {
    Gson().fromJson<List<RecordedIncident>>(sharedPreferences.getString("history_${getUserId()}", "[]"), object : TypeToken<List<RecordedIncident>>() {}.type) ?: emptyList()
  }.getOrDefault(emptyList())

  fun recordIncident(incident: Incident) {
    val userId = getUserId() ?: return
    val received = incident.broadcasterId == userId
    if (!received && incident.responses.none { it.responderId == userId && it.status != "DECLINED" }) return
    val record = RecordedIncident(incident.id, incident.address.orEmpty(), incident.status,
      incident.timeline.lastOrNull()?.timestamp?.take(10).orEmpty(), received)
    val history = (listOf(record) + getIncidentHistory().filterNot { it.id == record.id }).take(100)
    sharedPreferences.edit().putString("history_$userId", Gson().toJson(history)).apply()
  }

  fun saveServerBaseUrl(url: String) {
    sharedPreferences.edit().putString(KEY_SERVER_BASE_URL, url).apply()
  }

  fun getServerBaseUrl(): String? = sharedPreferences.getString(KEY_SERVER_BASE_URL, null)

  fun saveGeminiApiKey(key: String) {
    sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
  }

  fun getGeminiApiKey(): String? = sharedPreferences.getString(KEY_GEMINI_API_KEY, null)

  @Synchronized
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
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_CACHED_USER_PROFILE = "cached_user_profile"
  }
}
