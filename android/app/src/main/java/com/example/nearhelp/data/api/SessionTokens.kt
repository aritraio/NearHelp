package com.example.nearhelp.data.api

import android.util.Base64
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.models.TokenRefreshRequest
import com.google.gson.JsonParser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Refresh before reconnecting so an incident can stay active beyond the access-token lifetime. */
object SessionTokens {
    private val mutex = Mutex()
    suspend fun bearer(): String = mutex.withLock {
        val storage = NearHelpApplication.instance.tokenStorage
        var access = storage.getAccessToken() ?: error("Sign in or start an emergency guest session.")
        val expires = runCatching {
            val payload = String(Base64.decode(access.split('.')[1], Base64.URL_SAFE or Base64.NO_WRAP))
            JsonParser.parseString(payload).asJsonObject.get("exp").asLong
        }.getOrDefault(0L)
        if (expires <= System.currentTimeMillis() / 1000 + 60) {
            val refresh = storage.getRefreshToken() ?: error("Your session expired. Please sign in again.")
            val response = RetrofitClient.getAuthApiService().refreshToken(TokenRefreshRequest(refresh))
            val session = response.body()
            check(response.isSuccessful && session != null) { "Your session expired. Please sign in again." }
            storage.saveSession(session.accessToken, session.refreshToken, session.user)
            access = session.accessToken
        }
        "Bearer $access"
    }
}
