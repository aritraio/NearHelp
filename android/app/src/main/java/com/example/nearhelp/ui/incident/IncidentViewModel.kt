package com.example.nearhelp.ui.incident

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.*
import com.example.nearhelp.data.api.ws.*
import com.example.nearhelp.data.location.UserLocationState
import com.example.nearhelp.ui.tracking.TrackingSnapshotDto
import com.example.nearhelp.ui.tracking.ResponderTrackingUpdateDto
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import retrofit2.Response
import java.util.UUID

data class IncidentUiState(
    val incident: Incident? = null,
    val feed: List<Incident> = emptyList(),
    val snapshot: TrackingSnapshotDto? = null,
    val connection: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val busy: Boolean = false,
    val error: String? = null,
    val role: String = "guest",
)

class IncidentViewModel(private val savedState: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private val api get() = RetrofitClient.getSosApiService()
    private val storage get() = NearHelpApplication.instance.tokenStorage
    private val socket = LiveTrackingWebSocketClient()
    private val gson = Gson()
    private val _state = MutableStateFlow(IncidentUiState())
    val state = _state.asStateFlow()
    private var poll: Job? = null
    private var requestJob: Job? = null
    private var connectedId: String? = null
    val userId get() = storage.getUserId()
    val activeId: String? get() = savedState["incidentId"]

    init {
        viewModelScope.launch { socket.connectionStatus.collect { status ->
            _state.update { it.copy(connection = status, error = if (status == ConnectionStatus.CONNECTED) null else it.error) }
            if (status == ConnectionStatus.CONNECTED) refreshIncident()
        } }
        viewModelScope.launch { socket.incomingEvents.collect { event ->
            when (event) {
                is TrackingIncomingEvent.ConnectionAckReceived -> _state.update { it.copy(role = event.role) }
                is TrackingIncomingEvent.SnapshotReceived -> runCatching {
                    val snapshot = gson.fromJson(event.rawJson, TrackingSnapshotDto::class.java)
                    _state.update { it.copy(snapshot = snapshot) }
                }
                is TrackingIncomingEvent.ResponderUpdateReceived -> runCatching {
                    val responder = gson.fromJson(event.rawJson, ResponderTrackingUpdateDto::class.java)
                    _state.update { current -> current.copy(snapshot = current.snapshot?.let { snapshot ->
                        snapshot.copy(responders = snapshot.responders.filterNot { it.responderId == responder.responderId } + responder)
                    }) }
                }
                is TrackingIncomingEvent.TimelineEventReceived -> refreshIncident()
                is TrackingIncomingEvent.RawMessageReceived -> if (event.type == "new_message") refreshIncident()
                is TrackingIncomingEvent.ErrorReceived -> _state.update { it.copy(error = event.message) }
                else -> Unit
            }
        } }
    }

    private suspend fun token(): String = SessionTokens.bearer()

    private fun <T> checked(response: Response<T>): T {
        if (!response.isSuccessful) {
            val detail = runCatching { gson.fromJson(response.errorBody()?.string(), Map::class.java)["detail"] }.getOrNull()
            error(detail?.toString() ?: "Server returned ${response.code()}. Please try again.")
        }
        return response.body() ?: error("The server returned an empty response.")
    }

    private fun action(block: suspend () -> Unit) {
        if (requestJob?.isActive == true) return
        requestJob = viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(error = e.message ?: "Could not reach the server.") } }
            finally { _state.update { it.copy(busy = false) } }
        }
    }

    fun start(initialId: String) {
        val id = activeId ?: initialId.takeIf { it.isNotBlank() }
            ?: storage.getActiveIncidentId()
        if (id != null) open(id) else browse()
    }

    fun browse() {
        poll?.cancel()
        connectedId = null
        socket.disconnect()
        savedState["incidentId"] = null as String?
        _state.update { IncidentUiState(error = it.error) }
        poll = viewModelScope.launch {
            while (isActive) {
                try { _state.update { it.copy(feed = checked(api.active(token(), null, null)).events, error = null) } }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { _state.update { it.copy(error = e.message) } }
                delay(10_000)
            }
        }
    }

    fun open(id: String) {
        if (runCatching { UUID.fromString(id) }.isFailure) {
            _state.update { it.copy(error = "Invalid incident ID. Open an incident from the feed.") }
            return
        }
        savedState["incidentId"] = id
        if (connectedId == id) return
        poll?.cancel()
        socket.disconnect()
        connectedId = id
        _state.value = IncidentUiState()
        poll = viewModelScope.launch {
            while (isActive) {
                try {
                    val access = token().removePrefix("Bearer ")
                    if (_state.value.connection in listOf(ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR)) socket.connect(id, access)
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { _state.update { it.copy(error = e.message) } }
                loadIncident(id)
                socket.requestSnapshot()
                delay(5_000)
            }
        }
    }

    private suspend fun loadIncident(id: String) {
        try {
            val detail = checked(api.detail(id))
            if (activeId == id) _state.update { it.copy(incident = detail) }
            if (detail.status in listOf("RESOLVED", "CANCELLED") && storage.getActiveIncidentId() == id) storage.saveActiveIncidentId(null)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { _state.update { it.copy(error = e.message) } }
    }

    fun refreshIncident() { activeId?.let { id -> viewModelScope.launch { loadIncident(id) } } }

    fun create(location: UserLocationState, category: String, notes: String, address: String) = action {
        check(location.hasFix && android.os.SystemClock.elapsedRealtime() - location.fixElapsedMillis < 120_000) { "Wait for a current GPS fix before sending SOS." }
        val key = savedState.get<String>("createKey") ?: storage.getPendingSosKey() ?: UUID.randomUUID().toString().also { savedState["createKey"] = it }
        // Preserve the same request on retries after a timeout; the backend replays its result.
        val request = (savedState.get<String>("createPayload") ?: storage.getPendingSosPayload())?.let { gson.fromJson(it, SosCreateRequest::class.java) }
            ?: SosCreateRequest(location.latitude, location.longitude, category, notes, address).also { savedState["createPayload"] = gson.toJson(it) }
        storage.savePendingSos(key, gson.toJson(request))
        val created = checked(api.create(token(), key, request))
        storage.savePendingSos(null, null)
        storage.saveActiveIncidentId(created.id)
        savedState["createKey"] = null as String?
        savedState["createPayload"] = null as String?
        open(created.id)
    }

    fun respond(location: UserLocationState) = action {
        val id = activeId ?: error("Select an incident first.")
        check(_state.value.incident?.broadcasterId != userId) { "You cannot respond to your own SOS." }
        checked(api.respond(id, token(), SosRespondRequest(latitude = location.takeIf { it.hasFix }?.latitude, longitude = location.takeIf { it.hasFix }?.longitude)))
        socket.connect(id, token().removePrefix("Bearer "))
        loadIncident(id)
    }

    fun finish(cancel: Boolean) = action {
        val id = activeId ?: error("Select an incident first.")
        if (cancel) checked(api.cancel(id, token()))
        else checked(api.resolve(id, token(), mapOf("resolution_notes" to "Resolved from Android", "resolved_by" to "victim")))
        loadIncident(id)
        socket.requestSnapshot()
    }

    fun updateLocation(location: UserLocationState, available: Boolean) {
        if (!location.hasFix || android.os.SystemClock.elapsedRealtime() - location.fixElapsedMillis >= 120_000) return
        viewModelScope.launch {
            try { checked(api.location(token(), DeviceLocation(location.latitude, location.longitude, available))) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(error = "Location could not be shared: ${e.message}") } }
        }
        if (_state.value.role == "responder" && _state.value.incident?.status !in listOf("RESOLVED", "CANCELLED")) {
            socket.sendLocationUpdate(location.latitude, location.longitude, accuracy = location.accuracyMeters)
        }
    }

    fun send(text: String): Boolean {
        val clean = text.trim()
        if (clean.isEmpty() || clean.length > 1000) return false
        if (!socket.sendChatMessage(clean)) {
            _state.update { it.copy(error = "Chat is disconnected. Your message has not been sent; retry after reconnecting.") }
            return false
        }
        return true
    }

    fun reconnect() { activeId?.let { id -> viewModelScope.launch {
        try { socket.connect(id, token().removePrefix("Bearer ")) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { _state.update { it.copy(error = e.message) } }
    } } }
    override fun onCleared() { socket.close(); super.onCleared() }
}
