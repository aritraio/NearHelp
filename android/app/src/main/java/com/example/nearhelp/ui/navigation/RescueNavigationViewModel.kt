package com.example.nearhelp.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.data.api.ws.LiveTrackingWebSocketClient
import com.example.nearhelp.data.repository.IRoutingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RescueNavigationViewModel(
  private val routingRepository: IRoutingRepository,
  private val wsClient: LiveTrackingWebSocketClient = LiveTrackingWebSocketClient()
) : ViewModel() {

  private val _uiState = MutableStateFlow(RescueNavigationUiState())
  val uiState: StateFlow<RescueNavigationUiState> = _uiState.asStateFlow()

  private var gpsSimulationJob: Job? = null

  init {
    observeWebSocketConnection()
    loadRoutes()
  }

  private fun observeWebSocketConnection() {
    viewModelScope.launch {
      wsClient.connectionStatus.collect { status ->
        _uiState.update { it.copy(connectionStatus = status) }
      }
    }
  }

  fun connectToTracking(incidentId: String = "KOL-SOS-8821", token: String? = null) {
    _uiState.update { it.copy(incidentId = incidentId) }
    wsClient.connect(incidentId, token)
  }

  fun loadRoutes(
    originLat: Double = _uiState.value.responderLat,
    originLng: Double = _uiState.value.responderLng,
    destLat: Double = _uiState.value.victimLat,
    destLng: Double = _uiState.value.victimLng,
    travelMode: String = _uiState.value.travelMode
  ) {
    _uiState.update { it.copy(isLoadingRoute = true) }
    viewModelScope.launch {
      val result = routingRepository.fetchRescueDirections(
        originLat = originLat,
        originLng = originLng,
        destLat = destLat,
        destLng = destLng,
        travelMode = travelMode,
        avoidHazards = _uiState.value.isTrafficAvoidanceEnabled,
        includeAedDetour = true,
        aedLat = 22.5806,
        aedLng = 88.4385,
        aedName = "Godrej Waterside Tower 1 Lobby AED"
      )

      result.onSuccess { data ->
        _uiState.update { current ->
          val activeRoute = when (current.selectedRouteType) {
            "DETOUR" -> data.detourRoute ?: data.primaryRoute
            "AED_PICKUP" -> data.aedPickupRoute ?: data.primaryRoute
            else -> data.primaryRoute
          }
          current.copy(
            primaryRoute = data.primaryRoute,
            detourRoute = data.detourRoute,
            aedPickupRoute = data.aedPickupRoute,
            recommendation = data.recommendation,
            liveDistanceFormatted = activeRoute.distanceFormatted,
            liveEtaFormatted = activeRoute.durationFormatted,
            isLoadingRoute = false
          )
        }
      }.onFailure {
        _uiState.update { it.copy(isLoadingRoute = false) }
      }
    }
  }

  fun selectRoute(routeType: String) {
    _uiState.update { current ->
      val selected = when (routeType) {
        "DETOUR" -> current.detourRoute ?: current.primaryRoute
        "AED_PICKUP" -> current.aedPickupRoute ?: current.primaryRoute
        else -> current.primaryRoute
      }
      current.copy(
        selectedRouteType = routeType,
        currentTurnStepIndex = 0,
        liveDistanceFormatted = selected.distanceFormatted,
        liveEtaFormatted = selected.durationFormatted
      )
    }
    showToast("📍 Switched to: ${getActiveRoute().routeName}")
  }

  fun toggleTrafficAvoidance() {
    val newAvoidance = !_uiState.value.isTrafficAvoidanceEnabled
    _uiState.update { it.copy(isTrafficAvoidanceEnabled = newAvoidance) }
    loadRoutes()
    showToast(if (newAvoidance) "⚡ AI Traffic & Hazard Avoidance: Enabled" else "Traffic Avoidance: Disabled")
  }

  fun getActiveRoute(): RescueRouteDto {
    val state = _uiState.value
    return when (state.selectedRouteType) {
      "DETOUR" -> state.detourRoute ?: state.primaryRoute
      "AED_PICKUP" -> state.aedPickupRoute ?: state.primaryRoute
      else -> state.primaryRoute
    }
  }

  fun nextTurnStep() {
    val activeRoute = getActiveRoute()
    val nextIdx = (_uiState.value.currentTurnStepIndex + 1).coerceAtMost(activeRoute.steps.size - 1)
    setTurnStepIndex(nextIdx)
  }

  fun prevTurnStep() {
    val prevIdx = (_uiState.value.currentTurnStepIndex - 1).coerceAtLeast(0)
    setTurnStepIndex(prevIdx)
  }

  fun setTurnStepIndex(index: Int) {
    val activeRoute = getActiveRoute()
    val step = activeRoute.steps.getOrNull(index) ?: return
    val polyPt = activeRoute.polylinePoints.getOrNull(index)

    _uiState.update { current ->
      current.copy(
        currentTurnStepIndex = index,
        liveEtaFormatted = step.durationFormatted.ifBlank { current.liveEtaFormatted },
        liveDistanceFormatted = step.distanceFormatted.ifBlank { current.liveDistanceFormatted }
      )
    }

    if (polyPt != null) {
      wsClient.sendLocationUpdate(
        latitude = polyPt.lat,
        longitude = polyPt.lng,
        heading = _uiState.value.liveBearingDeg,
        speedMps = if (index == activeRoute.steps.size - 1) 0.0 else 1.33,
        accuracy = 2.5f
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
      val activeRoute = getActiveRoute()
      val points = activeRoute.polylinePoints
      var step = _uiState.value.currentTurnStepIndex

      while (isActive && step < points.size) {
        val pt = points[step]
        val nextPt = points.getOrNull(step + 1)
        val heading = if (nextPt != null) {
          calculateHeading(pt.lat, pt.lng, nextPt.lat, nextPt.lng)
        } else {
          _uiState.value.liveBearingDeg
        }

        val speedMps = if (step == points.size - 1) 0.0 else 1.33

        wsClient.sendLocationUpdate(
          latitude = pt.lat,
          longitude = pt.lng,
          heading = heading,
          speedMps = speedMps,
          accuracy = 2.5f
        )

        val navStep = activeRoute.steps.getOrNull(step)
        _uiState.update { current ->
          current.copy(
            currentTurnStepIndex = step,
            responderLat = pt.lat,
            responderLng = pt.lng,
            liveBearingDeg = heading,
            liveEtaFormatted = navStep?.durationFormatted ?: current.liveEtaFormatted,
            liveDistanceFormatted = navStep?.distanceFormatted ?: current.liveDistanceFormatted
          )
        }

        if (step == points.size - 1) {
          _uiState.update { it.copy(incidentStatus = "ARRIVED", isGpsSimulationActive = false) }
          showToast("📍 Arrived on scene at Elevator Bank B.")
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

  fun onArrivedClick() {
    stopGpsSimulation()
    val activeRoute = getActiveRoute()
    _uiState.update {
      it.copy(
        incidentStatus = "ARRIVED",
        currentTurnStepIndex = activeRoute.steps.size - 1,
        liveEtaFormatted = "Arrived",
        liveDistanceFormatted = "0m"
      )
    }
    wsClient.sendStatusUpdate(status = "ARRIVED", note = "Arrived at Elevator Bank B Ground Concourse")
    showToast("📍 Broadcasted Arrival on scene to Victim & Dispatch.")
  }

  fun toggleMedicalId() {
    _uiState.update { it.copy(isMedicalIdRevealed = !it.isMedicalIdRevealed) }
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
    wsClient.sendStatusUpdate(status = "HANDOVER_108", note = "Transferred patient to 108 Emergency Medical Team")
    showToast("🚑 Patient handed over to 108 ALS Ambulance.")
  }

  fun onResolveClick() {
    _uiState.update { it.copy(incidentStatus = "RESOLVED") }
    wsClient.sendStatusUpdate(status = "RESOLVED", note = "Emergency resolved. AMRI ICU bed reserved.")
    showToast("✨ Rescue marked RESOLVED. Incident archived.")
  }

  fun showToast(msg: String) {
    _uiState.update { it.copy(toastMessage = msg) }
  }

  fun clearToast() {
    _uiState.update { it.copy(toastMessage = null) }
  }

  private fun calculateHeading(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2))
    val x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
      Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon)
    val brng = Math.toDegrees(Math.atan2(y, x))
    return ((brng + 360.0) % 360.0).toFloat()
  }

  override fun onCleared() {
    super.onCleared()
    stopGpsSimulation()
    wsClient.disconnect()
  }
}
