package com.example.nearhelp.data.places

import kotlin.math.*

const val NEARBY_RADIUS_METERS = 3_000.0
const val LOCATION_MAX_AGE_MILLIS = 120_000L

enum class NearbyCategory(val googleType: String, val label: String) {
    HOSPITALS("hospital", "Hospitals"), PHARMACIES("pharmacy", "Pharmacies"), POLICE("police", "Police stations")
}

data class GeoPoint(val latitude: Double, val longitude: Double) {
    val valid: Boolean get() = latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0
    fun distanceTo(other: GeoPoint): Double {
        val lat = Math.toRadians(other.latitude - latitude)
        val lng = Math.toRadians(other.longitude - longitude)
        val a = sin(lat / 2).pow(2) + cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(lng / 2).pow(2)
        return 6_371_000 * 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}

data class NearbyPlace(val id: String, val name: String, val address: String, val point: GeoPoint,
    val phone: String? = null, val mapsUrl: String? = null, val attributions: List<String> = emptyList())

data class NearbyResult(val place: NearbyPlace, val distanceMeters: Double)

/** Recheck provider results against the current device fix, not a city label or map viewport. */
fun nearbyOnly(places: List<NearbyPlace>, center: GeoPoint): List<NearbyResult> = places
    .filter { it.point.valid }.distinctBy { it.id }
    .map { NearbyResult(it, center.distanceTo(it.point)) }
    .filter { it.distanceMeters <= NEARBY_RADIUS_METERS }.sortedBy { it.distanceMeters }

fun usableLocation(point: GeoPoint, hasFix: Boolean, hasPermission: Boolean, fixMillis: Long, nowMillis: Long): Boolean =
    hasFix && hasPermission && point.valid && nowMillis - fixMillis in 0 until LOCATION_MAX_AGE_MILLIS

interface NearbyPlacesRepository {
    suspend fun search(center: GeoPoint, category: NearbyCategory, language: String): List<NearbyPlace>
}
