package com.example.nearhelp.data.places

import android.content.Context
import com.example.nearhelp.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Native Android SDK supports the same Android-restricted key as the map. No REST key in URLs. */
class GoogleNearbyPlacesRepository(private val context: Context) : NearbyPlacesRepository {
    override suspend fun search(center: GeoPoint, category: NearbyCategory, language: String): List<NearbyPlace> {
        Places.initializeWithNewPlacesApiEnabled(context.applicationContext, context.getString(R.string.google_maps_key), Locale.forLanguageTag(language))
        val client = Places.createClient(context)
        val cancellation = CancellationTokenSource()
        val request = SearchNearbyRequest.builder(
            CircularBounds.newInstance(LatLng(center.latitude, center.longitude), NEARBY_RADIUS_METERS),
            listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS,
                Place.Field.BUSINESS_STATUS, Place.Field.INTERNATIONAL_PHONE_NUMBER, Place.Field.GOOGLE_MAPS_URI)
        ).setIncludedTypes(listOf(category.googleType))
            .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE).setMaxResultCount(20)
            .setCancellationToken(cancellation.token).build()
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancellation.cancel() }
            client.searchNearby(request).addOnSuccessListener { response ->
                val results = response.places.mapNotNull { place ->
                    val point = place.location ?: return@mapNotNull null
                    val id = place.id ?: return@mapNotNull null
                    val name = place.displayName?.takeIf(String::isNotBlank) ?: return@mapNotNull null
                    if (place.businessStatus == Place.BusinessStatus.CLOSED_PERMANENTLY) return@mapNotNull null
                    NearbyPlace(id, name, place.formattedAddress.orEmpty(), GeoPoint(point.latitude, point.longitude),
                        place.internationalPhoneNumber, place.googleMapsUri?.toString(), place.attributions.orEmpty())
                }
                if (continuation.isActive) continuation.resume(results)
            }.addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }
}
