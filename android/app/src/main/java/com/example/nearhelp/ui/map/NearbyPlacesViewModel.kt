package com.example.nearhelp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.places.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NearbyPlacesState(val category: NearbyCategory = NearbyCategory.HOSPITALS,
    val center: GeoPoint? = null, val results: List<NearbyResult> = emptyList(), val loading: Boolean = false,
    val locationReady: Boolean = false, val failed: Boolean = false)

class NearbyPlacesViewModel(private val repository: NearbyPlacesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(NearbyPlacesState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null
    private var generation = 0
    private var searchedCenter: GeoPoint? = null
    private var searchedCategory: NearbyCategory? = null
    private var searchedLanguage: String? = null
    private var lastSearchMillis: Long? = null
    private var places: List<NearbyPlace> = emptyList()

    fun update(point: GeoPoint, hasFix: Boolean, hasPermission: Boolean, fixMillis: Long, nowMillis: Long,
               category: NearbyCategory, language: String, force: Boolean = false) {
        if (!usableLocation(point, hasFix, hasPermission, fixMillis, nowMillis)) {
            generation++; job?.cancel(); searchedCenter = null; places = emptyList()
            mutableState.value = NearbyPlacesState(category = category)
            return
        }
        val moved = searchedCenter?.distanceTo(point)?.let { it >= 200 } ?: true
        val changed = searchedCategory != category || searchedLanguage != language
        val expired = lastSearchMillis?.let { nowMillis - it >= 300_000 } ?: true
        mutableState.value = mutableState.value.copy(center = point, category = category, locationReady = true,
            results = if (moved || changed) emptyList() else nearbyOnly(places, point))
        // GPS updates arrive every second. Only search again on meaningful movement, category/language
        // changes, explicit refresh, or after five minutes. Never expand the radius to fill an empty list.
        if (!force && !moved && !changed && !expired) return
        job?.cancel()
        val requestGeneration = ++generation
        searchedCenter = point; searchedCategory = category; searchedLanguage = language; lastSearchMillis = nowMillis
        places = emptyList()
        mutableState.value = mutableState.value.copy(loading = true, failed = false, results = emptyList())
        job = viewModelScope.launch {
            try {
                val result = repository.search(point, category, language)
                if (generation == requestGeneration) {
                    places = result
                    mutableState.value = mutableState.value.copy(results = nearbyOnly(result, mutableState.value.center ?: point), loading = false)
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                if (generation == requestGeneration) mutableState.value = mutableState.value.copy(loading = false, failed = true, results = emptyList())
            }
        }
    }
}
