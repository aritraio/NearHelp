package com.example.nearhelp.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CommunityGeoMapUiState(
  val incident: SpatialIncident = SpatialIncident(),
  val enabledLayers: Set<MapLayerKey> = setOf(
    MapLayerKey.VICTIM,
    MapLayerKey.RESPONDERS,
    MapLayerKey.HOSPITALS,
    MapLayerKey.AEDS,
    MapLayerKey.POSTGIS_WAVE,
    MapLayerKey.ROUTES
  ),
  val selectedEntity: SelectedMapEntity? = null,
  val showSqlHud: Boolean = true,
  val activeResponderIndex: Int = 0,
  val zoomLevel: Float = 1.0f,
  val panOffsetX: Float = 0f,
  val panOffsetY: Float = 0f,
  val postgisQuerySnippet: String = """
    SELECT r.id, r.name, r.cpr_cert,
           ST_Distance(r.geom::geography, ST_MakePoint(88.4378, 22.5804)::geography) AS dist_m
    FROM responder_telemetry r
    WHERE ST_DWithin(r.geom::geography, ST_MakePoint(88.4378, 22.5804)::geography, 1500)
    ORDER BY dist_m ASC LIMIT 5;
  """.trimIndent(),
)

class CommunityGeoMapViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(CommunityGeoMapUiState())
  val uiState: StateFlow<CommunityGeoMapUiState> = _uiState.asStateFlow()

  fun toggleLayer(layer: MapLayerKey) {
    _uiState.update { state ->
      val currentLayers = state.enabledLayers
      val newLayers = if (currentLayers.contains(layer)) {
        currentLayers - layer
      } else {
        currentLayers + layer
      }
      state.copy(enabledLayers = newLayers)
    }
  }

  fun selectEntity(entity: SelectedMapEntity?) {
    _uiState.update { it.copy(selectedEntity = entity) }
  }

  fun clearSelectedEntity() {
    _uiState.update { it.copy(selectedEntity = null) }
  }

  fun toggleSqlHud() {
    _uiState.update { it.copy(showSqlHud = !it.showSqlHud) }
  }

  fun setZoom(zoom: Float) {
    _uiState.update { it.copy(zoomLevel = zoom.coerceIn(0.7f, 2.5f)) }
  }

  fun zoomIn() {
    _uiState.update { it.copy(zoomLevel = (it.zoomLevel + 0.2f).coerceAtMost(2.5f)) }
  }

  fun zoomOut() {
    _uiState.update { it.copy(zoomLevel = (it.zoomLevel - 0.2f).coerceAtLeast(0.7f)) }
  }

  fun updatePan(dx: Float, dy: Float) {
    _uiState.update {
      it.copy(
        panOffsetX = it.panOffsetX + dx,
        panOffsetY = it.panOffsetY + dy
      )
    }
  }

  fun resetView() {
    _uiState.update {
      it.copy(
        zoomLevel = 1.0f,
        panOffsetX = 0f,
        panOffsetY = 0f,
        selectedEntity = null
      )
    }
  }

  fun setActiveResponder(index: Int) {
    _uiState.update { it.copy(activeResponderIndex = index) }
  }
}
