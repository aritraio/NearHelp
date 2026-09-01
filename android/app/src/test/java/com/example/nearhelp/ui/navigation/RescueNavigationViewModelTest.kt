package com.example.nearhelp.ui.navigation

import com.example.nearhelp.data.repository.IRoutingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RescueNavigationViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeRoutingRepository
  private lateinit var viewModel: RescueNavigationViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeRoutingRepository()
    viewModel = RescueNavigationViewModel(fakeRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state loads primary and detour rescue routes`() = runTest {
    advanceUntilIdle()
    val state = viewModel.uiState.value

    assertNotNull(state.primaryRoute)
    assertNotNull(state.detourRoute)
    assertNotNull(state.aedPickupRoute)
    assertEquals("PRIMARY", state.selectedRouteType)
    assertEquals("340m", state.liveDistanceFormatted)
    assertEquals("2.5 mins", state.liveEtaFormatted)
  }

  @Test
  fun `selectRoute DETOUR switches active route and updates ETA`() = runTest {
    advanceUntilIdle()
    viewModel.selectRoute("DETOUR")

    val state = viewModel.uiState.value
    assertEquals("DETOUR", state.selectedRouteType)
    assertEquals("380m", state.liveDistanceFormatted)
    assertEquals("1.8 mins", state.liveEtaFormatted)
    assertEquals(0, state.currentTurnStepIndex)
  }

  @Test
  fun `selectRoute AED_PICKUP switches to AED route`() = runTest {
    advanceUntilIdle()
    viewModel.selectRoute("AED_PICKUP")

    val state = viewModel.uiState.value
    assertEquals("AED_PICKUP", state.selectedRouteType)
    assertEquals("410m", state.liveDistanceFormatted)
    assertEquals("2.8 mins", state.liveEtaFormatted)
  }

  @Test
  fun `nextTurnStep and prevTurnStep update step index within bounds`() = runTest {
    advanceUntilIdle()
    assertEquals(0, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.nextTurnStep()
    assertEquals(1, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.nextTurnStep()
    assertEquals(2, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.prevTurnStep()
    assertEquals(1, viewModel.uiState.value.currentTurnStepIndex)

    viewModel.prevTurnStep()
    viewModel.prevTurnStep() // should coerce to 0
    assertEquals(0, viewModel.uiState.value.currentTurnStepIndex)
  }

  @Test
  fun `toggleTrafficAvoidance toggles flag and reloads routes`() = runTest {
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isTrafficAvoidanceEnabled)

    viewModel.toggleTrafficAvoidance()
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isTrafficAvoidanceEnabled)

    viewModel.toggleTrafficAvoidance()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isTrafficAvoidanceEnabled)
  }

  @Test
  fun `toggleMedicalId toggles reveal state`() {
    val initial = viewModel.uiState.value.isMedicalIdRevealed
    viewModel.toggleMedicalId()
    assertEquals(!initial, viewModel.uiState.value.isMedicalIdRevealed)
  }

  @Test
  fun `toggleAedAttached and toggleCprMetronome update states`() {
    assertFalse(viewModel.uiState.value.isAedAttached)
    viewModel.toggleAedAttached()
    assertTrue(viewModel.uiState.value.isAedAttached)

    assertFalse(viewModel.uiState.value.isCprMetronomeActive)
    viewModel.toggleCprMetronome()
    assertTrue(viewModel.uiState.value.isCprMetronomeActive)
  }

  @Test
  fun `onArrivedClick sets status to ARRIVED and marks last turn step`() {
    viewModel.onArrivedClick()
    val state = viewModel.uiState.value

    assertEquals("ARRIVED", state.incidentStatus)
    assertEquals("Arrived", state.liveEtaFormatted)
    assertEquals("0m", state.liveDistanceFormatted)
    assertFalse(state.isGpsSimulationActive)
  }

  @Test
  fun `onHandover108Click and onResolveClick advance emergency lifecycle`() {
    viewModel.onHandover108Click()
    assertEquals("HANDOVER_108", viewModel.uiState.value.incidentStatus)

    viewModel.onResolveClick()
    assertEquals("RESOLVED", viewModel.uiState.value.incidentStatus)
  }
}

class FakeRoutingRepository : IRoutingRepository {
  override suspend fun fetchRescueDirections(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double,
    travelMode: String,
    avoidHazards: Boolean,
    includeAedDetour: Boolean,
    aedLat: Double?,
    aedLng: Double?,
    aedName: String?
  ): Result<DirectionsResponseData> {
    return Result.success(
      DirectionsResponseData(
        origin = RouteLocationPoint(originLat, originLng),
        destination = RouteLocationPoint(destLat, destLng),
        travelMode = travelMode,
        primaryRoute = DEFAULT_PRIMARY_RESCUE_ROUTE,
        detourRoute = DEFAULT_DETOUR_RESCUE_ROUTE,
        aedPickupRoute = DEFAULT_AED_RESCUE_ROUTE,
        activeHazardsCount = 2,
        recommendation = DEFAULT_RECOMMENDATION
      )
    )
  }

  override suspend fun fetchActiveHazards(): Result<List<RoadHazardDto>> {
    return Result.success(DEFAULT_HAZARDS)
  }

  override suspend fun reportRoadHazard(
    title: String,
    hazardType: String,
    severity: String,
    latitude: Double,
    longitude: Double,
    description: String
  ): Result<Boolean> {
    return Result.success(true)
  }
}
