package com.example.nearhelp.ui.map

import com.example.nearhelp.data.places.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyPlacesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val barasat = GeoPoint(22.722, 88.481)
    private val kolkata = GeoPoint(22.573, 88.364)
    private fun place(id: String, point: GeoPoint) = NearbyPlace(id, id, "", point)
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun barasatOnly_excludesKolkata_sortsNearestFirst_andDeduplicates() {
        val near = place("Barasat nearby", GeoPoint(22.723, 88.482))
        val farther = place("Barasat farther", GeoPoint(22.737, 88.482))
        val results = nearbyOnly(listOf(place("Kolkata", kolkata), farther, near, near), barasat)
        assertEquals(listOf(near.id, farther.id), results.map { it.place.id })
        assertTrue(results.all { it.distanceMeters < NEARBY_RADIUS_METERS })
    }

    @Test fun strictRadiusBoundaryAndInvalidCoordinatesAreRejected() {
        val justInside = place("inside", GeoPoint(barasat.latitude + 0.0269, barasat.longitude))
        val justOutside = place("outside", GeoPoint(barasat.latitude + 0.0271, barasat.longitude))
        val invalid = place("invalid", GeoPoint(Double.NaN, 88.0))
        assertEquals(listOf("inside"), nearbyOnly(listOf(justOutside, invalid, justInside), barasat).map { it.place.id })
    }

    @Test fun placeholderOldFutureOrUnauthorizedFixNeverSearches() = runTest {
        val repo = FakeRepository()
        val vm = NearbyPlacesViewModel(repo)
        vm.update(barasat, false, true, 100, 100, NearbyCategory.HOSPITALS, "en")
        vm.update(barasat, true, false, 100, 100, NearbyCategory.HOSPITALS, "en")
        vm.update(barasat, true, true, 100, 120100, NearbyCategory.HOSPITALS, "en")
        vm.update(barasat, true, true, 101, 100, NearbyCategory.HOSPITALS, "en")
        runCurrent()
        assertTrue(repo.calls.isEmpty())
        assertFalse(vm.state.value.locationReady)
    }

    @Test fun frequentGpsUpdatesDoNotRepeatBillableRequests_butCategoryAndLanguageChangesDo() = runTest {
        val repo = FakeRepository()
        val vm = NearbyPlacesViewModel(repo)
        vm.update(barasat, true, true, 100, 100, NearbyCategory.HOSPITALS, "en"); runCurrent()
        repeat(30) { index -> vm.update(barasat, true, true, 100 + index.toLong(), 100 + index.toLong(), NearbyCategory.HOSPITALS, "en") }
        runCurrent(); assertEquals(1, repo.calls.size)
        vm.update(barasat, true, true, 200, 200, NearbyCategory.POLICE, "en"); runCurrent()
        vm.update(barasat, true, true, 300, 300, NearbyCategory.POLICE, "bn"); runCurrent()
        assertEquals(3, repo.calls.size)
        assertEquals(listOf("hospital", "police", "police"), repo.calls.map { it.second.googleType })
    }

    @Test fun changingCitiesClearsOldPinsImmediately_andLateOldResponseCannotReplaceCurrentResults() = runTest {
        val oldResponse = CompletableDeferred<List<NearbyPlace>>()
        val newResponse = CompletableDeferred<List<NearbyPlace>>()
        val repo = object : NearbyPlacesRepository {
            override suspend fun search(center: GeoPoint, category: NearbyCategory, language: String): List<NearbyPlace> =
                withContext(NonCancellable) { if (center == barasat) oldResponse.await() else newResponse.await() }
        }
        val vm = NearbyPlacesViewModel(repo)
        vm.update(barasat, true, true, 100, 100, NearbyCategory.HOSPITALS, "en"); runCurrent()
        vm.update(kolkata, true, true, 200, 200, NearbyCategory.HOSPITALS, "en"); runCurrent()
        assertTrue(vm.state.value.results.isEmpty())
        newResponse.complete(listOf(place("new", kolkata))); runCurrent()
        oldResponse.complete(listOf(place("old", barasat))); runCurrent()
        assertEquals(listOf("new"), vm.state.value.results.map { it.place.id })
    }

    @Test fun lostOrStaleLocationClearsResults_andProviderFailureNeverAddsDirectoryFixtures() = runTest {
        val repo = FakeRepository(listOf(place("local", barasat)))
        val vm = NearbyPlacesViewModel(repo)
        vm.update(barasat, true, true, 100, 100, NearbyCategory.HOSPITALS, "en"); runCurrent()
        assertEquals(1, vm.state.value.results.size)
        vm.update(barasat, true, true, 100, 120100, NearbyCategory.HOSPITALS, "en"); runCurrent()
        assertTrue(vm.state.value.results.isEmpty())
        repo.failure = true
        vm.update(barasat, true, true, 130000, 130000, NearbyCategory.HOSPITALS, "en"); runCurrent()
        assertTrue(vm.state.value.failed)
        assertTrue(vm.state.value.results.isEmpty())
    }

    private class FakeRepository(val places: List<NearbyPlace> = emptyList()) : NearbyPlacesRepository {
        val calls = mutableListOf<Pair<GeoPoint, NearbyCategory>>()
        var failure = false
        override suspend fun search(center: GeoPoint, category: NearbyCategory, language: String): List<NearbyPlace> {
            calls += center to category
            if (failure) error("Google search unavailable")
            return places
        }
    }
}
