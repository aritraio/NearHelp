package com.example.nearhelp.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs

data class UserLocationState(
    val latitude: Double = 22.5726,
    val longitude: Double = 88.3639,
    val localityName: String = "Locating...",
    val coordinatesText: String = "22.5726° N 88.3639° E",
    val safetyIndex: Int = 92,
    val isLocating: Boolean = true,
    val hasPermission: Boolean = false
)

class LocationHelper(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _locationState = MutableStateFlow(
        UserLocationState(
            hasPermission = hasLocationPermission()
        )
    )
    val locationState: StateFlow<UserLocationState> = _locationState.asStateFlow()

    private var isListening = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateFromLocation(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _locationState.value = _locationState.value.copy(
                hasPermission = false,
                isLocating = false,
                localityName = "Location permission needed"
            )
            return
        }

        _locationState.value = _locationState.value.copy(
            hasPermission = true,
            isLocating = true
        )

        try {
            // 1. Get best last known location immediately
            val lastKnownGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastKnownNetwork = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val lastKnownPassive = locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            val bestLastLocation = listOfNotNull(lastKnownGps, lastKnownNetwork, lastKnownPassive)
                .maxByOrNull { it.time }

            if (bestLastLocation != null) {
                updateFromLocation(bestLastLocation)
            }

            // 2. Register for continuous location updates
            if (!isListening && locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000L,
                        5f,
                        locationListener
                    )
                    isListening = true
                }

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000L,
                        5f,
                        locationListener
                    )
                    isListening = true
                }
            }
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                hasPermission = false,
                isLocating = false
            )
        }
    }

    fun stopLocationUpdates() {
        if (isListening) {
            try {
                locationManager?.removeUpdates(locationListener)
            } catch (_: Exception) {}
            isListening = false
        }
    }

    private fun updateFromLocation(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        val coordText = formatCoordinates(lat, lon)

        // Deterministic safety score based on location coordinates hash
        val calculatedSafetyIndex = 88 + (abs((lat * 100).toInt() + (lon * 100).toInt()) % 11)

        _locationState.value = _locationState.value.copy(
            latitude = lat,
            longitude = lon,
            coordinatesText = coordText,
            safetyIndex = calculatedSafetyIndex.coerceIn(85, 99),
            isLocating = false,
            hasPermission = true
        )

        // Reverse geocode locality in background
        scope.launch(Dispatchers.IO) {
            val locality = reverseGeocode(lat, lon)
            withContext(Dispatchers.Main) {
                _locationState.value = _locationState.value.copy(
                    localityName = locality
                )
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): String {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    return formatAddressLocality(address)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    return formatAddressLocality(address)
                }
            }
        } catch (_: Exception) {}

        // Fallback geodetic locality formatting
        return "Current Location"
    }

    private fun formatAddressLocality(address: android.location.Address): String {
        val subLocality = address.subLocality ?: address.thoroughfare ?: address.featureName
        val locality = address.locality ?: address.subAdminArea ?: address.adminArea

        return when {
            !subLocality.isNullOrBlank() && !locality.isNullOrBlank() && subLocality != locality -> {
                "$subLocality, $locality"
            }
            !locality.isNullOrBlank() -> locality
            !subLocality.isNullOrBlank() -> subLocality
            !address.adminArea.isNullOrBlank() -> address.adminArea
            else -> "Current Location"
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        val latAbs = String.format(Locale.US, "%.4f° %s", abs(latitude), latDir)
        val lonAbs = String.format(Locale.US, "%.5f° %s", abs(longitude), lonDir)
        return "$latAbs $lonAbs"
    }
}
