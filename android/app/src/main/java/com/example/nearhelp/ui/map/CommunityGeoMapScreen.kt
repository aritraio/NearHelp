package com.example.nearhelp.ui.map

import com.example.nearhelp.i18n.LocalizedText as Text
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.data.places.*
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityGeoMapScreen(onNavigateBack: () -> Unit, onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToAssistant: () -> Unit = {}, onNavigateToProfile: () -> Unit = {}, onNavigateToTracking: () -> Unit = {},
    onNavigateToNavigation: () -> Unit = {}, modifier: Modifier = Modifier, viewModel: CommunityGeoMapViewModel = viewModel(), showBottomBar: Boolean = true) {
    val (location, requestLocation) = rememberCareLocation()
    val context = LocalContext.current
    val nearby: NearbyPlacesViewModel = viewModel { NearbyPlacesViewModel(GoogleNearbyPlacesRepository(context.applicationContext)) }
    val state by nearby.state.collectAsStateWithLifecycle()
    var category by rememberSaveable { mutableStateOf(NearbyCategory.HOSPITALS.name) }
    var query by rememberSaveable { mutableStateOf("") }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(Unit) { while (true) { delay(5_000); now = SystemClock.elapsedRealtime() } }
    val selectedCategory = NearbyCategory.valueOf(category)
    val language = LocalConfiguration.current.locales[0].toLanguageTag()
    LaunchedEffect(location, category, language, refresh, now) {
        nearby.update(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission,
            location.fixElapsedMillis, SystemClock.elapsedRealtime(), selectedCategory, language)
    }
    val results = state.results.filter { query.isBlank() || "${it.place.name} ${it.place.address}".contains(query, true) }
    val detail = results.firstOrNull { it.place.id == detailId }
    val icon = when (selectedCategory) { NearbyCategory.HOSPITALS -> Icons.Outlined.LocalHospital; NearbyCategory.PHARMACIES -> Icons.Outlined.LocalPharmacy; NearbyCategory.POLICE -> Icons.Outlined.LocalPolice }
    val pins = (if (detail == null) results else listOf(detail)).map { item ->
        MapPinItem(item.place.id, item.place.name, distanceLabel(item.distanceMeters), LatLng(item.place.point.latitude, item.place.point.longitude),
            if (selectedCategory == NearbyCategory.POLICE) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_GREEN)
    }
    fun refreshNearby() {
        requestLocation()
        nearby.update(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission,
            location.fixElapsedMillis, SystemClock.elapsedRealtime(), selectedCategory, language, force = true)
        refresh++
    }
    fun openMaps(place: NearbyPlace? = null) {
        val uri = if (place != null) Uri.parse(place.mapsUrl ?: "https://www.google.com/maps/search/?api=1&query=${Uri.encode(place.name)}&query_place_id=${Uri.encode(place.id)}")
        else Uri.parse("geo:${location.latitude},${location.longitude}?q=${Uri.encode(selectedCategory.googleType)}&z=15")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
    BackHandler(enabled = detailId != null) { detailId = null }
    Column(modifier.fillMaxSize()) {
        CarePage(if (detail != null) "Place details" else null, if (detailId != null) ({ detailId = null }) else null, Modifier.weight(1f)) {
            if (detail == null) {
                CareHeading("Nearby help", "Hospitals, pharmacies and police within 3 km.")
                CareField("Search nearby results", query, { query = it }, hint = "Name or address nearby")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NearbyCategory.entries.forEach { item -> FilterChip(category == item.name,
                        onClick = { category = item.name; detailId = null }, label = { Text(item.label) }) }
                }
            }
            CareMap(location, pins = pins, onPinClick = { detailId = it.id }, nearbyRadiusMeters = NEARBY_RADIUS_METERS)
            CareButton(if (location.hasPermission) "Refresh nearby places" else "Allow location access", ::refreshNearby, icon = Icons.Outlined.MyLocation, enabled = !state.loading)
            if (!state.locationReady) {
                CareNote("Current location needed", "Turn on location and wait for a recent GPS fix. Nearby results use your current position.", Icons.Outlined.MyLocation)
            } else if (state.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                CareMuted("Finding nearby places on Google Maps…")
            } else if (state.failed) {
                CareNote("Nearby search unavailable", "We couldn’t load places from Google Maps. Try again or open Google Maps.", Icons.Outlined.WifiOff, error = true)
                CareButton("Open Google Maps", { openMaps() }, icon = Icons.Outlined.Map)
            } else if (detail != null) {
                val place = detail.place
                CareHeading(place.name, place.address)
                CareMuted("${distanceLabel(detail.distanceMeters)} away · Google Maps")
                CareNote("Confirm availability", "Call the facility to confirm opening hours and emergency services.")
                CareButton("Get directions", {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.point.latitude},${place.point.longitude}&destination_place_id=${Uri.encode(place.id)}")))
                }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Navigation)
                place.phone?.takeIf(String::isNotBlank)?.let { number -> CareButton("Call this place", { dial(context, number) }, icon = Icons.Outlined.Phone) }
                CareButton("View on Google Maps", { openMaps(place) }, icon = Icons.Outlined.Map)
                place.attributions.forEach { attribution -> ProviderAttribution(attribution) }
            } else {
                Text(selectedCategory.label, style = MaterialTheme.typography.titleMedium)
                CareMuted("Within 3 km · closest first · straight-line distance")
                if (results.isEmpty()) CareNote("No matching places within 3 km", "Try another category or clear the search. The search area stays close to you.")
                results.forEach { result -> CareRow(result.place.name, "${distanceLabel(result.distanceMeters)} · ${result.place.address}", icon) { detailId = result.place.id } }
                results.flatMap { it.place.attributions }.distinct().forEach { attribution -> ProviderAttribution(attribution) }
                CareMuted("Places from Google Maps. Call to confirm hours and availability.")
                EmergencyCallButton()
            }
        }
        if (showBottomBar) VictimBottomNavBar(VictimNavTab.MAP, { when (it) { VictimNavTab.HOME -> onNavigateToHome(); VictimNavTab.CHAT -> onNavigateToAssistant(); VictimNavTab.MAP -> {}; VictimNavTab.PROFILE -> onNavigateToProfile() } })
    }
}

private fun distanceLabel(meters: Double): String = if (meters < 1000) "${meters.toInt()} m" else String.format(Locale.getDefault(), "%.1f km", meters / 1000)

/** Preserve clickable provider-credit links supplied by Google Places. */
@Composable
private fun ProviderAttribution(html: String) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()
    AndroidView(modifier = Modifier.fillMaxWidth(), factory = { android.widget.TextView(it).apply {
        textSize = 14f; movementMethod = android.text.method.LinkMovementMethod.getInstance()
    } }, update = { view ->
        view.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        view.setTextColor(color); view.setLinkTextColor(linkColor)
    })
}
