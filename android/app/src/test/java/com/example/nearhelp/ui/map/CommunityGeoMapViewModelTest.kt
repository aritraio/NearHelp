package com.example.nearhelp.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CommunityGeoMapViewModelTest {

  private lateinit var viewModel: CommunityGeoMapViewModel

  @Before
  fun setUp() {
    viewModel = CommunityGeoMapViewModel()
  }

  @Test
  fun `initial state contains all standard map layers enabled`() {
    val state = viewModel.uiState.value
    assertTrue(state.enabledLayers.contains(MapLayerKey.VICTIM))
    assertTrue(state.enabledLayers.contains(MapLayerKey.RESPONDERS))
    assertTrue(state.enabledLayers.contains(MapLayerKey.HOSPITALS))
    assertTrue(state.enabledLayers.contains(MapLayerKey.AEDS))
    assertTrue(state.enabledLayers.contains(MapLayerKey.POSTGIS_WAVE))
    assertTrue(state.enabledLayers.contains(MapLayerKey.ROUTES))
    assertEquals(1.0f, state.zoomLevel)
    assertEquals(0f, state.panOffsetX)
    assertEquals(0f, state.panOffsetY)
    assertNull(state.selectedEntity)
    assertTrue(state.showSqlHud)
  }

  @Test
  fun `toggleLayer correctly disables and enables target layer`() {
    // Disable Hospitals layer
    viewModel.toggleLayer(MapLayerKey.HOSPITALS)
    assertFalse(viewModel.uiState.value.enabledLayers.contains(MapLayerKey.HOSPITALS))

    // Re-enable Hospitals layer
    viewModel.toggleLayer(MapLayerKey.HOSPITALS)
    assertTrue(viewModel.uiState.value.enabledLayers.contains(MapLayerKey.HOSPITALS))
  }

  @Test
  fun `selectEntity and clearSelectedEntity update state properly`() {
    val sampleHospital = viewModel.uiState.value.incident.hospitals.first()
    viewModel.selectEntity(SelectedMapEntity.HospitalEntity(sampleHospital))

    val selected = viewModel.uiState.value.selectedEntity
    assertNotNull(selected)
    assertTrue(selected is SelectedMapEntity.HospitalEntity)
    assertEquals(sampleHospital.name, (selected as SelectedMapEntity.HospitalEntity).hospital.name)

    // Clear selection
    viewModel.clearSelectedEntity()
    assertNull(viewModel.uiState.value.selectedEntity)
  }

  @Test
  fun `zoomIn, zoomOut and resetView maintain bounded zoom and pan offsets`() {
    // Zoom in
    viewModel.zoomIn()
    assertEquals(1.2f, viewModel.uiState.value.zoomLevel, 0.001f)

    // Zoom out
    viewModel.zoomOut()
    assertEquals(1.0f, viewModel.uiState.value.zoomLevel, 0.001f)

    // Pan update
    viewModel.updatePan(25f, -15f)
    assertEquals(25f, viewModel.uiState.value.panOffsetX, 0.001f)
    assertEquals(-15f, viewModel.uiState.value.panOffsetY, 0.001f)

    // Reset view
    viewModel.resetView()
    assertEquals(1.0f, viewModel.uiState.value.zoomLevel, 0.001f)
    assertEquals(0f, viewModel.uiState.value.panOffsetX, 0.001f)
    assertEquals(0f, viewModel.uiState.value.panOffsetY, 0.001f)
  }

  @Test
  fun `toggleSqlHud toggles boolean flag`() {
    assertTrue(viewModel.uiState.value.showSqlHud)
    viewModel.toggleSqlHud()
    assertFalse(viewModel.uiState.value.showSqlHud)
    viewModel.toggleSqlHud()
    assertTrue(viewModel.uiState.value.showSqlHud)
  }

  @Test
  fun `incident data contains valid Kolkata coordinates and facilities`() {
    val incident = viewModel.uiState.value.incident
    assertEquals(22.5804, incident.lat, 0.0001)
    assertEquals(88.4378, incident.lng, 0.0001)
    assertTrue(incident.responders.isNotEmpty())
    assertTrue(incident.hospitals.isNotEmpty())
    assertTrue(incident.aeds.isNotEmpty())
  }
}
