package com.example.nearhelp.ui.incident

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.nearhelp.data.api.*
import com.example.nearhelp.data.api.ws.*
import com.example.nearhelp.data.location.UserLocationState
import com.example.nearhelp.data.repository.*
import com.example.nearhelp.ui.navigation.RouteLocationPoint
import com.example.nearhelp.ui.tracking.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val stores = mutableListOf<ViewModelStore>()
    private val id = "f6bf9fde-6198-44dd-8ee8-75466dbf9971"
    private val user = "owner"
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { stores.forEach { it.clear() }; Dispatchers.resetMain() }
    private fun vm(repo: FakeRepository, socket: FakeSocket = FakeSocket(), clock: () -> Long = { 10_000L }): IncidentViewModel =
        IncidentViewModel(SavedStateHandle(), repo, socket, clock).also { model -> stores += ViewModelStore().also { it.put("incident", model) } }
    private fun incidentTest(block: suspend TestScope.() -> Unit) = runTest {
        try { block() } finally { stores.forEach { it.clear() }; stores.clear(); runCurrent() }
    }
    private fun location(fix: Long = 10_000, permission: Boolean = true) = UserLocationState(latitude = 22.722, longitude = 88.481, hasFix = true, hasPermission = permission, fixElapsedMillis = fix)
    private fun incident() = Incident(id = id, broadcasterId = user, status = "SOS_TRIGGERED", latitude = 22.722, longitude = 88.481)

    @Test fun freshLocationRequired_andChoosingNeverReportsSent() = incidentTest {
        val repo = FakeRepository(incident()); val model = vm(repo)
        model.beginCreate(); assertEquals(SosSubmission.CHOOSING, model.state.value.submission)
        model.create(location(permission = false), "medical", GENERIC_MEDICAL_SOS, "Barasat"); runCurrent()
        assertEquals(SosSubmission.WAITING_FOR_LOCATION, model.state.value.submission)
        model.create(location(fix = -200000), "medical", GENERIC_MEDICAL_SOS, "Barasat"); runCurrent()
        model.create(location(fix = 20000), "medical", GENERIC_MEDICAL_SOS, "Barasat"); runCurrent()
        assertTrue(repo.created.isEmpty())
        model.create(location(), "medical", GENERIC_MEDICAL_SOS, "Barasat"); runCurrent()
        assertEquals(1, repo.created.size)
        assertEquals(id, model.state.value.incident?.id)
        assertEquals(SosSubmission.NONE, model.state.value.submission)
        assertNotNull(repo.activeId)
    }

    @Test fun failedPostRetainsPayloadAndKey_retryDoesNotCreateNewRequestOrUseNewCoordinates() = incidentTest {
        val repo = FakeRepository(incident()).apply { failCreate = true }; val model = vm(repo)
        model.beginCreate(); model.create(location(), "medical", "Chest pain", "Original landmark"); runCurrent()
        assertEquals(SosSubmission.FAILED, model.state.value.submission)
        val original = repo.pending!!
        assertNull(model.state.value.incident)
        repo.failCreate = false
        model.retryPending(); runCurrent()
        assertEquals(listOf(original, original), repo.created)
        assertNull(repo.pending)
        assertEquals("Chest pain", model.state.value.pendingDescription ?: repo.created.last().request.description)
    }

    @Test fun repeatedLocationUpdatesAndDoubleTapsDispatchOnlyOnePost() = incidentTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeRepository(incident()).apply { createGate = gate }; val model = vm(repo)
        model.beginCreate()
        repeat(10) { model.create(location(), "medical", "Breathing trouble", "Home") }
        runCurrent(); assertEquals(1, repo.created.size)
        assertNull(model.state.value.incident)
        gate.complete(Unit); runCurrent()
        model.create(location(), "medical", "New description", "New place"); runCurrent()
        assertEquals(1, repo.created.size)
    }

    @Test fun cancellingDuringPostWaitsForAcknowledgement_thenCancelsThatIncident() = incidentTest {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeRepository(incident()).apply { createGate = gate }; val model = vm(repo)
        model.beginCreate(); model.create(location(), "medical", GENERIC_MEDICAL_SOS, "Home"); runCurrent()
        model.finish(true)
        assertTrue(model.state.value.cancellationPending)
        assertTrue(repo.finished.isEmpty())
        gate.complete(Unit); runCurrent()
        assertEquals(listOf(id to true), repo.finished)
        assertEquals("CANCELLED", model.state.value.incident?.status)
        assertNull(repo.activeId)
        assertFalse(repo.cancelPending)
    }

    @Test fun processRestorationResumesSamePendingPost_andRetainsRequestedCancellation() = incidentTest {
        val repo = FakeRepository(incident()).apply {
            pending = PendingMedicalSos("existing-key", SosCreateRequest(22.722, 88.481, "medical", "Original", "Home"))
            cancelPending = true
        }
        val model = vm(repo); model.beginCreate(); runCurrent()
        assertEquals("existing-key", repo.created.single().key)
        assertEquals("CANCELLED", model.state.value.terminalStatus)
        assertEquals(SosSubmission.NONE, model.state.value.submission)
    }

    @Test fun cancellationFailureKeepsActiveIncidentUntilServerConfirmation() = incidentTest {
        val repo = FakeRepository(incident()).apply { activeId = id; failFinish = true }; val model = vm(repo)
        model.start(id); runCurrent(); model.finish(true); runCurrent()
        assertEquals(id, repo.activeId)
        assertTrue(model.state.value.cancellationPending)
        assertNull(model.state.value.terminalStatus)
        assertNotNull(model.state.value.error)
        repo.failFinish = false; model.finish(true); runCurrent()
        assertEquals("CANCELLED", model.state.value.terminalStatus)
        assertNull(repo.activeId)
    }

    @Test fun returningThroughTrackingResumesCancellationAfterCreateWasAlreadyAcknowledged() = incidentTest {
        val repo = FakeRepository(incident()).apply { activeId = id; cancelPending = true }
        val model = vm(repo)
        model.start(""); runCurrent()
        assertEquals(listOf(id to true), repo.finished)
        assertEquals("CANCELLED", model.state.value.terminalStatus)
        assertNull(repo.activeId)
        assertFalse(repo.cancelPending)
    }

    @Test fun resolveAcknowledgementEndsImmediatelyEvenIfLaterDetailIsUnavailable() = incidentTest {
        val repo = FakeRepository(incident()).apply { activeId = id }; val model = vm(repo)
        model.start(id); runCurrent(); repo.failDetail = true
        model.finish(false); runCurrent()
        assertEquals("RESOLVED", model.state.value.terminalStatus)
        assertNull(repo.activeId)
        assertEquals(RescuePhase.RESOLVED, presentIncident(model.state.value, 10000, 0).phase)
    }

    @Test fun restTrackingWorksWhenSocketIsDown_andStationaryFixRefreshesReceipt() = incidentTest {
        val repo = FakeRepository(incident()).apply { activeId = id }
        repo.snapshot = TrackingSnapshotDto(incidentId = id, responders = listOf(ResponderTrackingUpdateDto(
            responderId = "helper", responderName = "Riya", latitude = 22.724, longitude = 88.48, status = "ACCEPTED", lastUpdated = "2026-11-03T00:00:00Z")))
        val model = vm(repo, clock = { testScheduler.currentTime })
        model.start(id); runCurrent()
        assertEquals("Riya", model.state.value.snapshot?.responders?.single()?.responderName)
        assertEquals(0L, model.state.value.lastTrackingUpdateElapsed)
        advanceTimeBy(5000); runCurrent()
        assertEquals(5000L, model.state.value.lastTrackingUpdateElapsed)
    }

    @Test fun arrivalUsesRestWithoutRequiringConnectedSocket() = incidentTest {
        val repo = FakeRepository(incident().copy(broadcasterId = "someone-else"))
        val model = vm(repo); model.start(id); runCurrent()
        model.markArrived(location()); runCurrent()
        assertEquals("ARRIVED", repo.responded.single().status)
    }

    private class FakeSocket : IncidentRealtime {
        override val connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
        override val incomingEvents = MutableSharedFlow<TrackingIncomingEvent>(extraBufferCapacity = 10)
        override fun connect(incidentId: String, token: String?) {}
        override fun disconnect(closeCode: Int, reason: String) {}
        override fun close() {}
        override fun requestSnapshot() = false
        override fun sendLocationUpdate(latitude: Double, longitude: Double, heading: Float?, speedMps: Double?, accuracy: Float?) = false
        override fun sendChatMessage(text: String, language: String) = false
        override fun sendStatusUpdate(status: String, note: String?) = false
    }
    private class FakeRepository(var value: Incident) : IncidentRepository {
        override val userId = "owner"
        var activeId: String? = null
        var pending: PendingMedicalSos? = null
        var cancelPending = false
        var failCreate = false; var failFinish = false; var failDetail = false
        var createGate: CompletableDeferred<Unit>? = null
        var snapshot = TrackingSnapshotDto(incidentId = value.id)
        val created = mutableListOf<PendingMedicalSos>()
        val finished = mutableListOf<Pair<String, Boolean>>()
        val responded = mutableListOf<SosRespondRequest>()
        override fun activeIncidentId() = activeId
        override fun saveActiveIncident(id: String?) { activeId = id }
        override fun pendingSos() = pending
        override fun savePendingSos(pending: PendingMedicalSos?) { this.pending = pending }
        override fun cancellationPending() = cancelPending
        override fun saveCancellationPending(value: Boolean) { cancelPending = value }
        override fun record(incident: Incident) {}
        override suspend fun socketToken() = "test"
        override suspend fun create(pending: PendingMedicalSos): Incident {
            created += pending; createGate?.await()
            if (failCreate) error("Network timeout")
            return value
        }
        override suspend fun detail(id: String): Incident { if (failDetail) error("Unavailable"); return value }
        override suspend fun tracking(id: String) = snapshot
        override suspend fun active() = emptyList<Incident>()
        override suspend fun respond(id: String, request: SosRespondRequest): IncidentResponse {
            responded += request
            return IncidentResponse(userId, "Helper", request.status).also { value = value.copy(responses = listOf(it)) }
        }
        override suspend fun finish(id: String, cancel: Boolean): String {
            finished += id to cancel
            if (failFinish) error("No confirmation")
            return if (cancel) "CANCELLED" else "RESOLVED"
        }
        override suspend fun location(value: DeviceLocation) {}
        override suspend fun route(originLat: Double, originLng: Double, destinationLat: Double, destinationLng: Double) = emptyList<RouteLocationPoint>()
    }
}
