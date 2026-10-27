package com.example.nearhelp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nearhelp.MainActivity
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.R
import com.example.nearhelp.data.api.RetrofitClient
import com.example.nearhelp.data.api.SessionTokens
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import java.util.UUID

object PushRegistration {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun sync() {
        val app = NearHelpApplication.instance
        if (FirebaseApp.getApps(app).isEmpty()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            app.tokenStorage.saveFcmToken(token)
            register(token)
        }
    }
    fun register(token: String) {
        scope.launch {
            if (!NearHelpApplication.instance.tokenStorage.isLoggedIn()) return@launch
            runCatching { RetrofitClient.getSosApiService().registerPush(SessionTokens.bearer(), mapOf("fcm_token" to token)) }
        }
    }
    fun configured(): Boolean = FirebaseApp.getApps(NearHelpApplication.instance).isNotEmpty()
}

class NearHelpMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        NearHelpApplication.instance.tokenStorage.saveFcmToken(token)
        PushRegistration.register(token)
    }
    override fun onMessageReceived(message: RemoteMessage) {
        val id = message.data["sos_event_id"] ?: return
        if (runCatching { UUID.fromString(id) }.isFailure) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(CHANNEL, "Emergency alerts", NotificationManager.IMPORTANCE_HIGH))
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("incident_id", id)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(this, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(id.hashCode(), NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.data["title"] ?: "NearHelp emergency")
            .setContentText(message.data["body"] ?: "Open the incident to view details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pending).setAutoCancel(true).build())
    }
    companion object { const val CHANNEL = "nearhelp_emergencies" }
}
