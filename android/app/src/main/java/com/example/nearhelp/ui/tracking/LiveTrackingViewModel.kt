package com.example.nearhelp.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.data.api.ws.LiveTrackingWebSocketClient
import com.example.nearhelp.data.api.ws.TrackingIncomingEvent
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveTrackingViewModel(
  private val wsClient: LiveTrackingWebSocketClient = LiveTrackingWebSocketClient(),
  private val gson: Gson = Gson()
) : ViewModel() {

  private val _uiState = MutableStateFlow(LiveTrackingUiState())
  val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

  private var gpsSimulationJob: Job? = null

  // Simulated GPS Waypoints along Sector V route to Godrej Waterside
  private val simulationWaypoints = listOf(
    Triple(22.5835, 88.4410, 34f),  // Sector V Ring Rd (Start)
    Triple(22.5822, 88.4398, 45f),  // Godrej Waterside Access Rd
    Triple(22.5810, 88.4385, 90f),  // Tower 1 Concourse
    Triple(22.5804, 88.4378, 0f)    // Elevator Lobby (Arrival)
  )

  init {
    observeWebSocketConnection()
    observeWebSocketEvents()
  }

  private fun observeWebSocketConnection() {
    viewModelScope.launch {
      wsClient.connectionStatus.collect { status ->
        _uiState.update { it.copy(connectionStatus = status) }
      }
    }
  }

  private fun observeWebSocketEvents() {
    viewModelScope.launch {
      wsClient.incomingEvents.collect { event ->
        when (event) {
          is TrackingIncomingEvent.SnapshotReceived -> {
            try {
              val snapshot = gson.fromJson(event.rawJson, TrackingSnapshotDto::class.java)
              applySnapshot(snapshot)
            } catch (e: Exception) {
              // Graceful fallback
            }
          }
          is TrackingIncomingEvent.ResponderUpdateReceived -> {
            try {
              val update = gson.fromJson(event.rawJson, ResponderTrackingUpdateDto::class.java)
              applyResponderUpdate(update)
            } catch (e: Exception) {
              // Graceful fallback
            }
          }
          is TrackingIncomingEvent.TimelineEventReceived -> {
            try {
              val timelineItem = gson.fromJson(event.rawJson, TimelineTrackingEventDto::class.java)
              _uiState.update { current ->
                current.copy(timelineEvents = current.timelineEvents + timelineItem)
              }
            } catch (e: Exception) {
              // Graceful fallback
            }
          }
          is TrackingIncomingEvent.ErrorReceived -> {
            showToast("⚠️ WS Error: ${event.message}")
          }
          else -> Unit
        }
      }
    }
  }

  fun connectToIncident(incidentId: String = "KOL-SOS-8821", token: String? = null) {
    _uiState.update { it.copy(incidentId = incidentId) }
    wsClient.connect(incidentId, token)
  }

  private fun applySnapshot(snapshot: TrackingSnapshotDto) {
    _uiState.update { current ->
      current.copy(
        incidentId = snapshot.incidentId.ifBlank { current.incidentId },
        incidentStatus = snapshot.status.ifBlank { current.incidentStatus },
        crisisType = snapshot.crisisType.ifBlank { current.crisisType },
        subType = snapshot.subType ?: current.subType,
        victimAddress = snapshot.incidentAddress ?: current.victimAddress,
        victimLat = snapshot.incidentLatitude,
        victimLng = snapshot.incidentLongitude,
        responders = if (snapshot.responders.isNotEmpty()) snapshot.responders else current.responders,
        closestAed = snapshot.closestAed ?: current.closestAed,
        closestHospital = snapshot.closestHospital ?: current.closestHospital
      )
    }
  }

  private fun applyResponderUpdate(update: ResponderTrackingUpdateDto) {
    _uiState.update { current ->
      val updatedResponders = current.responders.map {
        if (it.responderId == update.responderId) update else it
      }.ifEmpty { listOf(update) }

      current.copy(
        responders = updatedResponders,
        liveSpeedKmh = update.speedKmh ?: current.liveSpeedKmh,
        liveBearingDeg = update.heading ?: current.liveBearingDeg,
        liveBearingCompass = update.bearingCompass ?: current.liveBearingCompass,
        liveEtaFormatted = update.etaFormatted,
        liveDistanceFormatted = update.distanceFormatted,
        incidentStatus = if (update.status == "ARRIVED") "ARRIVED" else current.incidentStatus
      )
    }
  }

  fun toggleGpsSimulation() {
    if (gpsSimulationJob?.isActive == true) {
      stopGpsSimulation()
    } else {
      startGpsSimulation()
    }
  }

  fun startGpsSimulation() {
    gpsSimulationJob?.cancel()
    _uiState.update { it.copy(isGpsSimulationActive = true) }

    gpsSimulationJob = viewModelScope.launch {
      var step = _uiState.value.currentTurnStepIndex
      while (isActive && step < simulationWaypoints.size) {
        val (lat, lng, heading) = simulationWaypoints[step]

        // Send real location update over WebSocket stream
        val speedMps = if (step == simulationWaypoints.size - 1) 0.0 else 1.33 // ~4.8 km/h
        wsClient.sendLocationUpdate(
          latitude = lat,
          longitude = lng,
          heading = heading,
          speedMps = speedMps,
          accuracy = 2.5f
        )

        val navStep = DEFAULT_NAVIGATION_STEPS.getOrNull(step)
        _uiState.update { current ->
          current.copy(
            currentTurnStepIndex = step,
            liveEtaFormatted = navStep?.eta ?: current.liveEtaFormatted,
            liveDistanceFormatted = navStep?.distance ?: current.liveDistanceFormatted
          )
        }

        if (step == simulationWaypoints.size - 1) {
          // Arrived on scene
          _uiState.update { it.copy(incidentStatus = "ARRIVED", isGpsSimulationActive = false) }
          break
        }

        delay(3_000)
        step++
      }
    }
  }

  fun stopGpsSimulation() {
    gpsSimulationJob?.cancel()
    gpsSimulationJob = null
    _uiState.update { it.copy(isGpsSimulationActive = false) }
  }

  fun nextTurnStep() {
    val nextIdx = (_uiState.value.currentTurnStepIndex + 1).coerceAtMost(DEFAULT_NAVIGATION_STEPS.size - 1)
    setTurnStepIndex(nextIdx)
  }

  fun prevTurnStep() {
    val prevIdx = (_uiState.value.currentTurnStepIndex - 1).coerceAtLeast(0)
    setTurnStepIndex(prevIdx)
  }

  private fun setTurnStepIndex(index: Int) {
    val step = DEFAULT_NAVIGATION_STEPS.getOrNull(index) ?: return
    val waypoint = simulationWaypoints.getOrNull(index)

    _uiState.update { current ->
      current.copy(
        currentTurnStepIndex = index,
        liveEtaFormatted = step.eta,
        liveDistanceFormatted = step.distance
      )
    }

    if (waypoint != null) {
      val (lat, lng, heading) = waypoint
      wsClient.sendLocationUpdate(
        latitude = lat,
        longitude = lng,
        heading = heading,
        speedMps = if (index == DEFAULT_NAVIGATION_STEPS.size - 1) 0.0 else 1.33,
        accuracy = 2.5f
      )
    }
  }

  fun onArrivedClick() {
    stopGpsSimulation()
    _uiState.update {
      it.copy(
        incidentStatus = "ARRIVED",
        currentTurnStepIndex = DEFAULT_NAVIGATION_STEPS.size - 1,
        liveEtaFormatted = "Arrived",
        liveDistanceFormatted = "0m"
      )
    }
    wsClient.sendStatusUpdate(status = "ARRIVED", note = "Arrived at Elevator Bank B Ground Concourse")
    showToast("📍 Broadcasted Arrival on scene to Victim & Dispatch.")
  }

  fun toggleAedAttached() {
    val newState = !_uiState.value.isAedAttached
    _uiState.update { it.copy(isAedAttached = newState) }
    wsClient.sendActionLog(
      actionType = if (newState) "aed_attached" else "aed_detached",
      details = mapOf("rhythm" to "Normal Sinus", "shocks_delivered" to 1)
    )
    showToast(if (newState) "⚡ AED Attached — Rhythm: Normal Sinus" else "AED Detached")
  }

  fun toggleCprMetronome() {
    val newState = !_uiState.value.isCprMetronomeActive
    _uiState.update { it.copy(isCprMetronomeActive = newState) }
    if (newState) {
      showToast("💓 CPR Metronome Active: 110 BPM (545ms interval)")
    }
  }

  fun onHandover108Click() {
    _uiState.update { it.copy(incidentStatus = "HANDOVER_108") }
    wsClient.sendStatusUpdate(status = "HANDOVER_108", note = "Patient transferred to AMRI ALS Ambulance Unit 4")
    showToast("🚑 Patient successfully handed over to 108 Emergency Medical Team.")
  }

  fun onResolveClick() {
    _uiState.update { it.copy(incidentStatus = "RESOLVED") }
    wsClient.sendStatusUpdate(status = "RESOLVED", note = "Cardiac emergency stabilized. AMRI ICU bed reserved.")
    showToast("✨ Rescue marked RESOLVED. Incident archived.")
  }

  fun toggleMedicalId() {
    _uiState.update { it.copy(isMedicalIdRevealed = !it.isMedicalIdRevealed) }
  }

  fun showToast(message: String) {
    _uiState.update { it.copy(toastMessage = message) }
  }

  fun clearToast() {
    _uiState.update { it.copy(toastMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    stopGpsSimulation()
    wsClient.disconnect()
  }
}
