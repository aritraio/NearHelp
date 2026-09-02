package com.example.nearhelp.data.api.ws

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
  DISCONNECTED,
  CONNECTING,
  CONNECTED,
  RECONNECTING,
  ERROR
}

sealed class TrackingIncomingEvent {
  data class SnapshotReceived(val rawJson: String, val jsonObject: JsonObject) : TrackingIncomingEvent()
  data class ResponderUpdateReceived(val rawJson: String, val jsonObject: JsonObject) : TrackingIncomingEvent()
  data class TimelineEventReceived(val rawJson: String, val jsonObject: JsonObject) : TrackingIncomingEvent()
  data class ConnectionAckReceived(val rawJson: String, val connectionId: String, val role: String) : TrackingIncomingEvent()
  data class PongReceived(val serverTime: Double) : TrackingIncomingEvent()
  data class ErrorReceived(val code: String, val message: String) : TrackingIncomingEvent()
  data class RawMessageReceived(val type: String, val rawJson: String) : TrackingIncomingEvent()
}

class LiveTrackingWebSocketClient(
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .pingInterval(15, TimeUnit.SECONDS)
    .build(),
  private val gson: Gson = Gson(),
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) {

  private val tag = "LiveTrackingWS"

  private var webSocket: WebSocket? = null
  private var currentIncidentId: String? = null
  private var currentToken: String? = null

  private fun isRunningOnEmulator(): Boolean {
    return (android.os.Build.FINGERPRINT.startsWith("generic")
        || android.os.Build.FINGERPRINT.startsWith("unknown")
        || android.os.Build.MODEL.contains("google_sdk")
        || android.os.Build.MODEL.contains("Emulator")
        || android.os.Build.MODEL.contains("Android SDK built for x86")
        || android.os.Build.MANUFACTURER.contains("Genymotion")
        || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
        || "google_sdk" == android.os.Build.PRODUCT)
  }

  companion object {
    const val DEFAULT_PRODUCTION_WS_URL: String = "wss://nearhelp-backend-7sfj.onrender.com"
  }

  private val defaultWsUrl: String
    get() = DEFAULT_PRODUCTION_WS_URL

  private var currentBaseWsUrl: String? = null

  private fun getEffectiveWsUrl(): String {
    return currentBaseWsUrl ?: defaultWsUrl
  }

  private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
  val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

  private val _incomingEvents = MutableSharedFlow<TrackingIncomingEvent>(extraBufferCapacity = 64)
  val incomingEvents: SharedFlow<TrackingIncomingEvent> = _incomingEvents.asSharedFlow()

  private var heartbeatJob: Job? = null
  private var reconnectJob: Job? = null
  private var shouldAutoReconnect = true

  fun setBaseWsUrl(url: String) {
    currentBaseWsUrl = if (url.startsWith("http://")) {
      url.replace("http://", "ws://").trimEnd('/')
    } else if (url.startsWith("https://")) {
      url.replace("https://", "wss://").trimEnd('/')
    } else {
      url.trimEnd('/')
    }
  }

  fun connect(incidentId: String, token: String? = null) {
    currentIncidentId = incidentId
    currentToken = token
    shouldAutoReconnect = true

    disconnect(closeCode = 1000, reason = "Reconnecting to new session")
    _connectionStatus.value = ConnectionStatus.CONNECTING

    val wsUrl = buildString {
      append("${getEffectiveWsUrl()}/ws/tracking/$incidentId")
      if (!token.isNullOrBlank()) {
        append("?token=$token")
      }
    }


    logI("Connecting to WebSocket URL: $wsUrl")

    val request = Request.Builder()
      .url(wsUrl)
      .build()

    webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
      override fun onOpen(webSocket: WebSocket, response: Response) {
        _connectionStatus.value = ConnectionStatus.CONNECTED
        startHeartbeat()
        logI("WebSocket connection established.")
      }

      override fun onMessage(webSocket: WebSocket, text: String) {
        scope.launch {
          handleIncomingMessage(text)
        }
        logD("Incoming WS Frame: $text")
      }

      override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        stopHeartbeat()
        logI("WebSocket closing (code=$code, reason=$reason)")
      }

      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        stopHeartbeat()
        logI("WebSocket closed (code=$code, reason=$reason)")
        scheduleReconnect()
      }

      override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _connectionStatus.value = ConnectionStatus.ERROR
        stopHeartbeat()
        logE("WebSocket failure: ${t.message}", t)
        _incomingEvents.tryEmit(TrackingIncomingEvent.ErrorReceived("CONNECTION_FAILURE", t.message ?: "Connection Failure"))
        scheduleReconnect()
      }
    })
  }

  private suspend fun handleIncomingMessage(text: String) {
    try {
      val json = JsonParser.parseString(text).asJsonObject
      val type = json.get("type")?.asString ?: "unknown"

      val event = when (type) {
        "connection_ack" -> {
          val connId = json.get("connection_id")?.asString ?: ""
          val role = json.get("role")?.asString ?: "victim"
          TrackingIncomingEvent.ConnectionAckReceived(text, connId, role)
        }
        "tracking_snapshot" -> {
          TrackingIncomingEvent.SnapshotReceived(text, json)
        }
        "responder_update" -> {
          TrackingIncomingEvent.ResponderUpdateReceived(text, json)
        }
        "timeline_event" -> {
          TrackingIncomingEvent.TimelineEventReceived(text, json)
        }
        "pong" -> {
          val sTime = json.get("server_time")?.asDouble ?: 0.0
          TrackingIncomingEvent.PongReceived(sTime)
        }
        "error" -> {
          val code = json.get("code")?.asString ?: "ERROR"
          val msg = json.get("message")?.asString ?: "Unknown error"
          TrackingIncomingEvent.ErrorReceived(code, msg)
        }
        else -> {
          TrackingIncomingEvent.RawMessageReceived(type, text)
        }
      }
      _incomingEvents.emit(event)
    } catch (e: Exception) {
      logE("Error parsing incoming WS message: ${e.message}", e)
    }
  }

  private fun startHeartbeat() {
    heartbeatJob?.cancel()
    heartbeatJob = scope.launch {
      while (isActive && _connectionStatus.value == ConnectionStatus.CONNECTED) {
        delay(15_000)
        sendPing()
      }
    }
  }

  private fun stopHeartbeat() {
    heartbeatJob?.cancel()
    heartbeatJob = null
  }

  private fun scheduleReconnect() {
    reconnectJob?.cancel()
    reconnectJob = scope.launch {
      _connectionStatus.value = ConnectionStatus.RECONNECTING
      delay(3_000)
      if (shouldAutoReconnect && currentIncidentId != null) {
        logI("Attempting auto-reconnect to incident: $currentIncidentId")
        connect(currentIncidentId!!, currentToken)
      }
    }
  }

  fun sendLocationUpdate(
    latitude: Double,
    longitude: Double,
    heading: Float? = null,
    speedMps: Double? = null,
    accuracy: Float? = null
  ): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "location_update")
      addProperty("latitude", latitude)
      addProperty("longitude", longitude)
      if (heading != null) addProperty("heading", heading)
      if (speedMps != null) addProperty("speed_mps", speedMps)
      if (accuracy != null) addProperty("accuracy", accuracy)
      addProperty("timestamp", System.currentTimeMillis() / 1000.0)
    }
    return sendMessage(payload.toString())
  }

  fun sendStatusUpdate(status: String, note: String? = null): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "status_update")
      addProperty("status", status)
      if (!note.isNullOrBlank()) addProperty("note", note)
    }
    return sendMessage(payload.toString())
  }

  fun sendActionLog(actionType: String, details: Map<String, Any> = emptyMap()): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "action_log")
      addProperty("action_type", actionType)
      add("details", gson.toJsonTree(details))
    }
    return sendMessage(payload.toString())
  }

  fun sendChatMessage(text: String, language: String = "en"): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "chat_message")
      addProperty("text", text)
      addProperty("language", language)
    }
    return sendMessage(payload.toString())
  }

  fun sendPing(): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "ping")
      addProperty("timestamp", System.currentTimeMillis() / 1000.0)
    }
    return sendMessage(payload.toString())
  }

  fun requestSnapshot(): Boolean {
    val payload = JsonObject().apply {
      addProperty("type", "get_snapshot")
    }
    return sendMessage(payload.toString())
  }

  private fun sendMessage(text: String): Boolean {
    val ws = webSocket
    if (ws != null && _connectionStatus.value == ConnectionStatus.CONNECTED) {
      return ws.send(text)
    }
    logW("Cannot send message: WebSocket is not in CONNECTED state.")
    return false
  }

  fun disconnect(closeCode: Int = 1000, reason: String = "Client Disconnected") {
    shouldAutoReconnect = false
    reconnectJob?.cancel()
    stopHeartbeat()
    try {
      webSocket?.close(closeCode, reason)
    } catch (e: Exception) {
      logW("Exception during socket close: ${e.message}")
    } finally {
      webSocket = null
      _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
  }

  private fun logD(msg: String) {
    try {
      Log.d(tag, msg)
    } catch (_: Throwable) {
      println("[$tag] $msg")
    }
  }

  private fun logI(msg: String) {
    try {
      Log.i(tag, msg)
    } catch (_: Throwable) {
      println("[$tag] $msg")
    }
  }

  private fun logW(msg: String) {
    try {
      Log.w(tag, msg)
    } catch (_: Throwable) {
      println("[$tag] $msg")
    }
  }

  private fun logE(msg: String, tr: Throwable? = null) {
    try {
      Log.e(tag, msg, tr)
    } catch (_: Throwable) {
      println("[$tag] $msg ${tr?.message ?: ""}")
    }
  }
}
