package com.example.nearhelp.ui.incident

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.api.*
import com.example.nearhelp.data.api.ws.*
import com.example.nearhelp.data.location.UserLocationState
import com.example.nearhelp.data.places.GeoPoint
import com.example.nearhelp.data.places.usableLocation
import com.example.nearhelp.data.repository.*
import com.example.nearhelp.ui.tracking.TrackingSnapshotDto
import com.example.nearhelp.ui.tracking.ResponderTrackingUpdateDto
import com.example.nearhelp.ui.navigation.RouteLocationPoint
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

enum class SosSubmission { NONE, CHOOSING, WAITING_FOR_LOCATION, SENDING, FAILED }

data class IncidentUiState(
    val incident: Incident? = null, val incidentId: String? = null,
    val feed: List<Incident> = emptyList(), val snapshot: TrackingSnapshotDto? = null,
    val connection: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val busy: Boolean = false, val error: String? = null, val syncError: String? = null,
    val role: String = "guest", val lastTrackingUpdateElapsed: Long? = null,
    val routePoints: List<RouteLocationPoint> = emptyList(),
    val submission: SosSubmission = SosSubmission.NONE,
    val pendingDescription: String? = null, val cancellationPending: Boolean = false,
    val terminalStatus: String? = null,
)

class IncidentViewModel(
    private val savedState: SavedStateHandle = SavedStateHandle(),
    private val repository: IncidentRepository = AndroidIncidentRepository(),
    private val socket: IncidentRealtime = LiveTrackingWebSocketClient(),
    private val elapsedTime: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {
    private val gson = Gson()
    private val mutableState = MutableStateFlow(IncidentUiState())
    val state = mutableState.asStateFlow()
    private var poll: Job? = null
    private var requestJob: Job? = null
    private var routeJob: Job? = null
    private var refreshJob: Job? = null
    private var creationInFlight = false
    private var connectedId: String? = null
    private var lastRouteRequestElapsed: Long? = null
    private var lastLocationPublish: Long? = null
    private var lastAvailability: Boolean? = null
    val userId get() = repository.userId
    val activeId: String? get() = mutableState.value.incidentId ?: savedState["incidentId"]

    init {
        viewModelScope.launch { socket.connectionStatus.collect { value ->
            mutableState.update { it.copy(connection = value) }
            if (value == ConnectionStatus.CONNECTED) { socket.requestSnapshot(); refreshIncident() }
        } }
        viewModelScope.launch { socket.incomingEvents.collect { event ->
            when (event) {
                is TrackingIncomingEvent.ConnectionAckReceived -> mutableState.update { it.copy(role = event.role) }
                is TrackingIncomingEvent.SnapshotReceived -> runCatching { applySnapshot(gson.fromJson(event.rawJson, TrackingSnapshotDto::class.java)) }
                is TrackingIncomingEvent.ResponderUpdateReceived -> runCatching {
                    val responder = gson.fromJson(event.rawJson, ResponderTrackingUpdateDto::class.java)
                    val incident = mutableState.value.incident ?: return@runCatching
                    val current = mutableState.value.snapshot ?: TrackingSnapshotDto(incidentId = incident.id,
                        status = incident.status, incidentLatitude = incident.latitude, incidentLongitude = incident.longitude)
                    applySnapshot(current.copy(responders = current.responders.filterNot { it.responderId == responder.responderId } + responder))
                }
                is TrackingIncomingEvent.TimelineEventReceived -> refreshIncident()
                is TrackingIncomingEvent.RawMessageReceived -> if (event.type == "new_message") refreshIncident()
                is TrackingIncomingEvent.ErrorReceived -> if (event.code != "CONNECTION_FAILURE") mutableState.update { it.copy(error = event.message) }
                else -> Unit
            }
        } }
    }

    fun beginCreate() {
        val existing = activeId ?: repository.activeIncidentId()
        when {
            existing != null -> { open(existing); if (repository.cancellationPending()) finish(true) }
            repository.pendingSos() != null -> retryPending()
            else -> mutableState.update { it.copy(submission = SosSubmission.CHOOSING) }
        }
    }

    fun start(initialId: String) {
        val id = activeId ?: initialId.takeIf(String::isNotBlank) ?: repository.activeIncidentId()
        if (id != null) {
            open(id)
            if (id == repository.activeIncidentId() && repository.cancellationPending()) finish(true)
        } else if (repository.pendingSos() != null) retryPending() else browse()
    }

    fun awaitLocation(description: String) {
        if (activeId == null && mutableState.value.submission in listOf(SosSubmission.CHOOSING, SosSubmission.WAITING_FOR_LOCATION))
            mutableState.update { it.copy(submission = SosSubmission.WAITING_FOR_LOCATION, pendingDescription = description) }
    }

    fun create(location: UserLocationState, category: String, notes: String, address: String) {
        if (activeId != null || requestJob?.isActive == true) return
        val existing = repository.pendingSos()
        if (existing != null) { submit(existing); return }
        if (!usableLocation(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission,
                location.fixElapsedMillis, elapsedTime())) { awaitLocation(notes); return }
        val pending = PendingMedicalSos(UUID.randomUUID().toString(), SosCreateRequest(location.latitude, location.longitude, "medical", notes, address))
        // Persist synchronously before starting HTTP. Process death cannot lose the retry key.
        repository.savePendingSos(pending)
        submit(pending)
    }

    fun retryPending() { repository.pendingSos()?.let(::submit) }

    private fun submit(pending: PendingMedicalSos) {
        if (requestJob?.isActive == true || activeId != null) return
        creationInFlight = true
        mutableState.update { it.copy(submission = SosSubmission.SENDING, pendingDescription = pending.request.description,
            busy = true, error = null, cancellationPending = repository.cancellationPending()) }
        requestJob = viewModelScope.launch {
            try {
                val response = repository.create(pending)
                check(validId(response.id)) { "The server did not confirm an incident ID. Retry the same SOS." }
                val created = response.copy(description = pending.request.description,
                    broadcasterId = response.broadcasterId ?: userId)
                repository.saveActiveIncident(created.id)
                repository.record(created)
                repository.savePendingSos(null)
                open(created.id, created)
                if (repository.cancellationPending()) completeFinish(created.id, true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableState.update { it.copy(submission = if (activeId == null) SosSubmission.FAILED else SosSubmission.NONE,
                error = e.message ?: "Could not reach the server.") } }
            finally { creationInFlight = false; mutableState.update { it.copy(busy = false) } }
        }
    }

    fun browse() {
        stopLive(); savedState["incidentId"] = null as String?
        mutableState.value = IncidentUiState()
        poll = viewModelScope.launch {
            while (isActive) {
                try { mutableState.update { it.copy(feed = repository.active(), syncError = null) } }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { mutableState.update { it.copy(syncError = e.message) } }
                delay(10_000)
            }
        }
    }

    private fun validId(id: String) = runCatching { UUID.fromString(id) }.isSuccess

    fun open(id: String, initial: Incident? = null) {
        if (!validId(id)) { mutableState.update { it.copy(error = "Invalid incident ID. Open an incident from the feed.") }; return }
        if (connectedId == id) return
        stopLive(); savedState["incidentId"] = id; connectedId = id
        mutableState.value = IncidentUiState(incident = initial, incidentId = id,
            busy = mutableState.value.busy, cancellationPending = id == repository.activeIncidentId() && repository.cancellationPending())
        poll = viewModelScope.launch {
            while (isActive) {
                if (mutableState.value.incident?.status in TERMINAL_STATUSES) break
                try {
                    if (mutableState.value.connection in listOf(ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR))
                        socket.connect(id, repository.socketToken())
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* REST polling remains available when the socket cannot connect. */ }
                synchronize(id)
                delay(5_000)
            }
        }
    }

    private suspend fun synchronize(id: String) = supervisorScope {
        val detail = async { runCatchingCancellable { repository.detail(id) } }
        val tracking = async { runCatchingCancellable { repository.tracking(id) } }
        val detailResult = detail.await(); val trackingResult = tracking.await()
        if (activeId != id) return@supervisorScope
        detailResult.getOrNull()?.let(::applyIncident)
        trackingResult.getOrNull()?.let(::applySnapshot)
        mutableState.update { it.copy(syncError = if (detailResult.isFailure && trackingResult.isFailure)
            "Live updates are unavailable. We’ll keep trying." else null) }
    }

    private suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> = try { Result.success(block()) }
        catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }

    private fun applyIncident(incident: Incident) {
        if (activeId != incident.id || ((mutableState.value.terminalStatus != null || mutableState.value.incident?.status in TERMINAL_STATUSES) && incident.status !in TERMINAL_STATUSES)) return
        mutableState.update { it.copy(incident = incident) }
        repository.record(incident)
        if (incident.status in TERMINAL_STATUSES) endLocally(incident.status)
    }

    private fun applySnapshot(snapshot: TrackingSnapshotDto) {
        if (snapshot.incidentId != activeId || mutableState.value.terminalStatus != null || mutableState.value.incident?.status in TERMINAL_STATUSES) return
        val previous = mutableState.value.snapshot?.responders.orEmpty().associateBy { it.responderId }
        val merged = snapshot.copy(responders = snapshot.responders.map { incoming ->
            val old = previous[incoming.responderId]
            if (old != null && telemetryTime(old.lastUpdated) > telemetryTime(incoming.lastUpdated)) old else incoming
        })
        // A stationary responder still sends fresh fixes. Receipt freshness is not coordinate movement.
        mutableState.update { it.copy(snapshot = merged, lastTrackingUpdateElapsed = elapsedTime()) }
        if (snapshot.status in TERMINAL_STATUSES) endLocally(snapshot.status) else updateRoute(merged)
    }

    fun refreshIncident() {
        val id = activeId ?: return
        if (refreshJob?.isActive == true || mutableState.value.incident?.status in TERMINAL_STATUSES) return
        refreshJob = viewModelScope.launch { synchronize(id) }
    }

    private fun action(block: suspend () -> Unit) {
        if (requestJob?.isActive == true) return
        mutableState.update { it.copy(busy = true, error = null) }
        requestJob = viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { mutableState.update { it.copy(error = e.message ?: "Could not reach the server.") } }
            finally { mutableState.update { it.copy(busy = false) } }
        }
    }

    fun respond(location: UserLocationState, status: String = "ACCEPTED") = action {
        val id = activeId ?: error("Select an incident first.")
        check(mutableState.value.incident?.broadcasterId != userId) { "You cannot respond to your own SOS." }
        val fresh = usableLocation(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission, location.fixElapsedMillis, elapsedTime())
        val response = repository.respond(id, SosRespondRequest(status, location.latitude.takeIf { fresh }, location.longitude.takeIf { fresh }))
        mutableState.update { current -> current.copy(incident = current.incident?.let { it.copy(responses = it.responses.filterNot { old -> old.responderId == response.responderId } + response) }) }
        socket.connect(id, repository.socketToken()); refreshIncident()
    }

    fun markArrived(location: UserLocationState) = respond(location, "ARRIVED")

    fun finish(cancel: Boolean) {
        if (cancel && (creationInFlight || (activeId == null && repository.pendingSos() != null))) {
            repository.saveCancellationPending(true)
            mutableState.update { it.copy(cancellationPending = true, error = null) }
            if (!creationInFlight) retryPending()
            return
        }
        val id = activeId ?: return
        if (cancel) { repository.saveCancellationPending(true); mutableState.update { it.copy(cancellationPending = true) } }
        action { completeFinish(id, cancel) }
    }

    private suspend fun completeFinish(id: String, cancel: Boolean) {
        val status = repository.finish(id, cancel)
        if (activeId == id) endLocally(status)
    }

    private fun endLocally(status: String) {
        val ownActive = repository.activeIncidentId() == activeId
        val cancellingThisIncident = mutableState.value.cancellationPending
        mutableState.update { it.copy(incident = it.incident?.copy(status = status), terminalStatus = status, cancellationPending = false,
            submission = SosSubmission.NONE, error = null, syncError = null, routePoints = emptyList()) }
        mutableState.value.incident?.let(repository::record)
        if (ownActive) repository.saveActiveIncident(null)
        if (ownActive || cancellingThisIncident) repository.saveCancellationPending(false)
        stopLive()
    }

    fun updateLocation(location: UserLocationState, available: Boolean) {
        val now = elapsedTime()
        if (!usableLocation(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission, location.fixElapsedMillis, now)) return
        val responding = mutableState.value.incident?.responses?.any { it.responderId == userId && it.status in ENGAGED_STATUSES } == true
        if (responding && mutableState.value.incident?.status !in TERMINAL_STATUSES)
            socket.sendLocationUpdate(location.latitude, location.longitude, accuracy = location.accuracyMeters)
        if (lastAvailability == available && lastLocationPublish?.let { now - it < 15_000 } == true) return
        lastLocationPublish = now; lastAvailability = available
        viewModelScope.launch {
            try { repository.location(DeviceLocation(location.latitude, location.longitude, available)) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* A later fix retries; a publishing failure must not replace SOS action errors. */ }
        }
    }

    fun send(text: String, language: String = "en"): Boolean {
        val clean = text.trim()
        if (clean.isEmpty() || clean.length > 1000) return false
        if (!socket.sendChatMessage(clean, language)) {
            mutableState.update { it.copy(error = "Chat is disconnected. Your message has not been sent; retry after reconnecting.") }; return false
        }
        return true
    }

    fun reconnect() { activeId?.let { id -> viewModelScope.launch {
        try { socket.connect(id, repository.socketToken()); refreshIncident() }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { mutableState.update { it.copy(syncError = e.message) } }
    } } }

    private fun updateRoute(snapshot: TrackingSnapshotDto) {
        val closest = snapshot.responders.filter { it.status in ENGAGED_STATUSES && validPosition(it) }
            .minByOrNull { it.distanceMeters } ?: return
        val now = elapsedTime()
        if (routeJob?.isActive == true || lastRouteRequestElapsed?.let { now - it < 30_000 } == true) return
        lastRouteRequestElapsed = now
        val id = activeId
        routeJob = viewModelScope.launch {
            try {
                val points = repository.route(closest.latitude, closest.longitude, snapshot.incidentLatitude, snapshot.incidentLongitude)
                if (activeId == id && mutableState.value.incident?.status !in TERMINAL_STATUSES) mutableState.update { it.copy(routePoints = points) }
            } catch (e: CancellationException) { throw e } catch (_: Exception) { /* Tracking continues without a route. */ }
        }
    }
    private fun stopLive() {
        poll?.cancel(); poll = null; refreshJob?.cancel(); refreshJob = null
        routeJob?.cancel(); routeJob = null; lastRouteRequestElapsed = null
        socket.disconnect(); connectedId = null
    }
    override fun onCleared() { socket.close(); super.onCleared() }
}
