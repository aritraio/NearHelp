package com.example.nearhelp.ui.map

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityGeoMapScreen(onNavigateBack: () -> Unit, onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToAssistant: () -> Unit = {}, onNavigateToProfile: () -> Unit = {}, onNavigateToTracking: () -> Unit = {},
    onNavigateToNavigation: () -> Unit = {}, modifier: Modifier = Modifier, viewModel: CommunityGeoMapViewModel = viewModel(), showBottomBar: Boolean = true) {
    val (location, requestLocation) = rememberCareLocation()
    val context = LocalContext.current
    var category by rememberSaveable { mutableStateOf("HOSPITALS") }
    var query by rememberSaveable { mutableStateOf("") }
    var allHours by rememberSaveable { mutableStateOf(true) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    val facilities = remember(location.latitude, location.longitude, location.hasFix) {
        if (location.hasFix) getAccurateKolkataRegionalFacilities(location.latitude, location.longitude, location.localityName) else emptyList()
    }
    val selectedCategory = EmergencyMapCategory.valueOf(category)
    val filtered = facilities.filter { it.category == selectedCategory && (query.isBlank() || "${it.name} ${it.address}".contains(query, true)) && (!allHours || it.subDetail.contains("24", true)) }.sortedBy { it.distanceKm }
    val detail = facilities.firstOrNull { it.id == detailId }
    val pins = (if (detail != null) listOf(detail) else filtered.take(15)).map { f -> MapPinItem(f.id, f.name, f.distance, LatLng(f.lat, f.lng), if (f.category == EmergencyMapCategory.POLICE) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_GREEN) }
    BackHandler(enabled = detailId != null) { detailId = null }
    Column(modifier.fillMaxSize()) {
        CarePage(if (detail != null) "${if (detail.category == EmergencyMapCategory.POLICE) "Police station" else if (detail.category == EmergencyMapCategory.PHARMACIES) "Pharmacy" else "Hospital"} details" else null,
            if (detail != null) ({ detailId = null }) else null, Modifier.weight(1f)) {
            if (detail == null) {
                CareField("Find hospitals, pharmacies or police", query, { query = it }, hint = "Search by name or area")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(EmergencyMapCategory.HOSPITALS, EmergencyMapCategory.PHARMACIES, EmergencyMapCategory.POLICE).forEach { c ->
                        FilterChip(category == c.name, onClick = { category = c.name }, label = { Text(if (c == EmergencyMapCategory.POLICE) "Police stations" else c.label) })
                    }
                }
                CareCheck("24-hour listings", allHours, { allHours = it })
            }
            CareMap(location, pins = pins, onPinClick = { detailId = it.id })
            if (!location.hasPermission) CareButton("Allow location access", requestLocation, icon = Icons.Outlined.MyLocation)
            if (detail == null) {
                Text("Nearest ${if (selectedCategory == EmergencyMapCategory.POLICE) "police stations" else selectedCategory.label.lowercase()}", style = MaterialTheme.typography.titleMedium)
                if (filtered.isEmpty()) {
                    CareNote(if (location.hasFix) "No directory results nearby" else "Waiting for your location", if (location.hasFix) "Search Maps for more facilities in your area." else "Allow location access to find nearby help.")
                    CareButton("Search in Maps", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.latitude},${location.longitude}?q=${Uri.encode(selectedCategory.label)}"))) }, icon = Icons.Outlined.Map)
                }
                filtered.forEach { f -> CareRow(f.name, "${f.distance} · ${f.address}", f.icon) { detailId = f.id } }
                CareMuted("Regional directory listings. Call to confirm opening hours and availability.")
                EmergencyCallButton()
            } else {
                CareHeading(detail.name, detail.address)
                CareMuted("${detail.distance} away · regional directory")
                if (detail.category == EmergencyMapCategory.HOSPITALS) CareNote("Hospital availability", "Call the hospital to confirm emergency care and bed availability.", Icons.Outlined.LocalHospital)
                CareButton("Get directions", { directions(context, LatLng(detail.lat, detail.lng), detail.name) }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Navigation)
                CareButton("Call ${if (detail.category == EmergencyMapCategory.POLICE) "police station" else if (detail.category == EmergencyMapCategory.PHARMACIES) "pharmacy" else "hospital"}", { dial(context, detail.phone) }, icon = Icons.Outlined.Phone)
            }
        }
        if (showBottomBar) VictimBottomNavBar(VictimNavTab.MAP, { when (it) { VictimNavTab.HOME -> onNavigateToHome(); VictimNavTab.CHAT -> onNavigateToAssistant(); VictimNavTab.MAP -> {}; VictimNavTab.PROFILE -> onNavigateToProfile() } })
    }
}

enum class EmergencyMapCategory(
  val label: String,
  val icon: ImageVector,
  val primaryColor: Color,
  val cardBg: Color,
  val cardBorder: Color,
) {
  ALL(
    label = "All",
    icon = Icons.Default.Shield,
    primaryColor = Color(0xFF2563EB),
    cardBg = Color(0xFFEFF6FF),
    cardBorder = Color(0xFFBFDBFE),
  ),
  HOSPITALS(
    label = "Hospitals",
    icon = Icons.Default.LocalHospital,
    primaryColor = Color(0xFFE11D48),
    cardBg = Color(0xFFFFF1F2),
    cardBorder = Color(0xFFFECDD3),
  ),
  POLICE(
    label = "Police",
    icon = Icons.Default.LocalPolice,
    primaryColor = Color(0xFF1D4ED8),
    cardBg = Color(0xFFEFF6FF),
    cardBorder = Color(0xFFBFDBFE),
  ),
  PHARMACIES(
    label = "Pharmacies",
    icon = Icons.Default.LocalPharmacy,
    primaryColor = Color(0xFF059669),
    cardBg = Color(0xFFECFDF5),
    cardBorder = Color(0xFFA7F3D0),
  ),
}

data class EmergencyMapFacility(
  val id: String,
  val name: String,
  val address: String = "",
  val category: EmergencyMapCategory,
  val distance: String,
  val distanceKm: Double,
  val status: String = "Open 24 hours",
  val subDetail: String,
  val bedAvailability: Int = 0,
  val totalBeds: Int = 0,
  val icuAvailability: Int = 0,
  val traumaLevel: String = "",
  val emergencyHelpline: String = "",
  val phone: String,
  val pinNormalizedX: Float = 0f,
  val pinNormalizedY: Float = 0f,
  val icon: ImageVector = Icons.Default.LocalHospital,
  val lat: Double = 22.5804,
  val lng: Double = 88.4378,
)

private data class FacilitySeed(
  val name: String,
  val address: String,
  val lat: Double,
  val lng: Double,
  val phone: String,
  val category: EmergencyMapCategory,
  val icon: ImageVector,
  val subDetail: String,
  val bedAvailability: Int = 0,
  val totalBeds: Int = 0,
  val icuAvailability: Int = 0,
  val traumaLevel: String = "",
  val emergencyHelpline: String = "",
)

/**
 * Computes geodesic distance in meters between two coordinates.
 */
private fun computeDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
  val results = FloatArray(1)
  android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
  return results[0].toDouble()
}

/**
 * Formats distance in meters or kilometers.
 */
private fun formatDistance(meters: Double): String {
  return if (meters < 1000.0) {
    "${meters.toInt()} m"
  } else {
    String.format(Locale.US, "%.1f km", meters / 1000.0)
  }
}


private fun getAccurateKolkataRegionalFacilities(
  userLat: Double,
  userLng: Double,
  localityName: String,
): List<EmergencyMapFacility> {
  val distToKolkata = computeDistanceMeters(userLat, userLng, 22.5726, 88.3639)

  val seeds: List<FacilitySeed> = if (distToKolkata <= 75_000.0) {
    listOf(
      // ==============================================================
      // 1. BARASAT (BARASAD) REGION HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Barasat Government Medical College & Hospital",
        address = "Banamalipur, Barasat, Kolkata, West Bengal 700124",
        lat = 22.7228,
        lng = 88.4815,
        phone = "033 2552 3228",
        emergencyHelpline = "033 2552 3211",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Medical College • 24/7 Trauma & Casualty",
        bedAvailability = 95,
        totalBeds = 850,
        icuAvailability = 16,
        traumaLevel = "Level 1 Apex Government Trauma Center"
      ),
      FacilitySeed(
        name = "Narayana Multispeciality Hospital — Barasat",
        address = "78, Jessore Road (South), Duckbunglow More, Barasat 700124",
        lat = 22.7180,
        lng = 88.4795,
        phone = "033 7122 2222",
        emergencyHelpline = "1800 309 0309",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Comprehensive Cardiac Center • 24/7 ER & Cath Lab",
        bedAvailability = 28,
        totalBeds = 150,
        icuAvailability = 6,
        traumaLevel = "Cardiac & Multi-Speciality Emergency"
      ),
      FacilitySeed(
        name = "Eco Hospital & Diagnostic Centre — Barasat",
        address = "NH 12, Purbachal, Nabapally, Barasat, Kolkata 700126",
        lat = 22.7155,
        lng = 88.4720,
        phone = "033 2584 1234",
        emergencyHelpline = "+91 98310 55667",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Critical Care ICU & Emergency Surgery",
        bedAvailability = 24,
        totalBeds = 120,
        icuAvailability = 4,
        traumaLevel = "Acute Care & Critical ICU Hub"
      ),
      FacilitySeed(
        name = "Care and Cure Hospital — Barasat",
        address = "Barrackpore Road, Champadali More, Barasat 700124",
        lat = 22.7245,
        lng = 88.4740,
        phone = "033 2562 8899",
        emergencyHelpline = "033 2562 8899",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Trauma Ward",
        bedAvailability = 18,
        totalBeds = 80,
        icuAvailability = 3,
        traumaLevel = "24/7 Casualty & Surgical Ward"
      ),

      // ==============================================================
      // 2. BARRACKPORE (BARAKPUR) REGION HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Dr. B. C. Roy General Hospital (Barrackpore Sub-Divisional)",
        address = "S. N. Banerjee Road, Barrackpore, Kolkata 700120",
        lat = 22.7610,
        lng = 88.3650,
        phone = "033 2592 0262",
        emergencyHelpline = "033 2592 0044",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Govt Sub-Divisional Hospital • 24/7 Emergency",
        bedAvailability = 55,
        totalBeds = 350,
        icuAvailability = 8,
        traumaLevel = "Sub-Divisional Trauma & Emergency Center"
      ),
      FacilitySeed(
        name = "Barrackpore Cantonment General Hospital",
        address = "Orderly Bazar, Barrackpore Cantonment, Barrackpore 700120",
        lat = 22.7665,
        lng = 88.3580,
        phone = "033 2592 0134",
        emergencyHelpline = "033 2592 0134",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Cantonment 24/7 Emergency & Acute Care Ward",
        bedAvailability = 22,
        totalBeds = 120,
        icuAvailability = 4,
        traumaLevel = "Cantonment Emergency Hospital"
      ),
      FacilitySeed(
        name = "College of Medicine & Sagore Dutta Hospital",
        address = "578, B.T. Road, Kamarhati, Belgharia, Kolkata 700058",
        lat = 22.6710,
        lng = 88.3765,
        phone = "033 2583 5555",
        emergencyHelpline = "033 2564 3000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Medical College • Level 1 Trauma Center",
        bedAvailability = 82,
        totalBeds = 700,
        icuAvailability = 14,
        traumaLevel = "Apex Government Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Panihati State General Hospital",
        address = "Barasat Road, Sodepur, Panihati, Kolkata 700110",
        lat = 22.7015,
        lng = 88.3795,
        phone = "033 2553 2211",
        emergencyHelpline = "033 2553 2211",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Govt 24/7 Emergency Casualty & Maternity Hospital",
        bedAvailability = 38,
        totalBeds = 250,
        icuAvailability = 5,
        traumaLevel = "Govt State General Hospital"
      ),
      FacilitySeed(
        name = "Balaram Seva Mandir State General Hospital (Khardah)",
        address = "B.T. Road, Khardah, Kolkata 700116",
        lat = 22.7160,
        lng = 88.3740,
        phone = "033 2553 2419",
        emergencyHelpline = "033 2553 2419",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Inpatient Wards",
        bedAvailability = 30,
        totalBeds = 150,
        icuAvailability = 4,
        traumaLevel = "State General Emergency Unit"
      ),

      // ==============================================================
      // 3. MADHYAMGRAM, BIRATI & DUM DUM CORRIDOR HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "Madhyamgram Sub-Divisional Hospital",
        address = "Sodepur-Madhyamgram Road, Madhyamgram, Kolkata 700129",
        lat = 22.7010,
        lng = 88.4550,
        phone = "033 2538 4455",
        emergencyHelpline = "033 2538 4455",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Ward & Acute Care Hub",
        bedAvailability = 22,
        totalBeds = 100,
        icuAvailability = 3,
        traumaLevel = "Sub-Divisional Emergency Center"
      ),
      FacilitySeed(
        name = "Midland Nursing Home & Hospital — Birati",
        address = "125, Jessore Road, Birati, Kolkata 700051",
        lat = 22.6685,
        lng = 88.4415,
        phone = "033 2514 1122",
        emergencyHelpline = "033 2514 1122",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Cardiac & Trauma Critical Care",
        bedAvailability = 14,
        totalBeds = 60,
        icuAvailability = 2,
        traumaLevel = "Critical Care Emergency Center"
      ),
      FacilitySeed(
        name = "ILS Hospitals — Dum Dum",
        address = "1, Mall Road, near Nagerbazar, Dum Dum, Kolkata 700080",
        lat = 22.6255,
        lng = 88.4195,
        phone = "033 4031 5555",
        emergencyHelpline = "033 4031 5555",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 ER • Multi-Speciality ICU & Cath Lab",
        bedAvailability = 26,
        totalBeds = 150,
        icuAvailability = 7,
        traumaLevel = "Trauma & Advanced Critical Care"
      ),
      FacilitySeed(
        name = "Spandan Emergency Hospital — Teghoria",
        address = "VIP Road, Teghoria, Kolkata 700059",
        lat = 22.6215,
        lng = 88.4350,
        phone = "033 2570 1234",
        emergencyHelpline = "033 2570 1234",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Emergency • 24/7 Cardiac Care Unit",
        bedAvailability = 16,
        totalBeds = 80,
        icuAvailability = 4,
        traumaLevel = "Level 1 Emergency Center"
      ),
      FacilitySeed(
        name = "Charnock Hospital — Teghoria / VIP Road",
        address = "BMC 195, Teghoria Major Arterial Rd, New Town, Kolkata 700157",
        lat = 22.6235,
        lng = 88.4390,
        phone = "033 4050 0500",
        emergencyHelpline = "033 4050 0500",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Tertiary Level 1 Trauma Center • Neuro & Cardiac ICU",
        bedAvailability = 34,
        totalBeds = 200,
        icuAvailability = 8,
        traumaLevel = "Tertiary Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Dum Dum Municipal Specialized Hospital",
        address = "Dum Dum Cantonment, Kolkata 700065",
        lat = 22.6320,
        lng = 88.4210,
        phone = "033 2549 2233",
        emergencyHelpline = "033 2549 2233",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty Ward",
        bedAvailability = 19,
        totalBeds = 100,
        icuAvailability = 3,
        traumaLevel = "24/7 Casualty & Acute Ward"
      ),

      // ==============================================================
      // 4. KOLKATA CORE, SALT LAKE & NEW TOWN HOSPITALS
      // ==============================================================
      FacilitySeed(
        name = "R. G. Kar Medical College and Hospital",
        address = "1, Khudiram Bose Sarani, Shyambazar, Kolkata 700004",
        lat = 22.6044,
        lng = 88.3770,
        phone = "033 2555 7656",
        emergencyHelpline = "108",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Govt Level 1 Trauma Center • 24/7 Blood Bank",
        bedAvailability = 140,
        totalBeds = 1200,
        icuAvailability = 22,
        traumaLevel = "Apex Level 1 Government Trauma Center"
      ),
      FacilitySeed(
        name = "Salt Lake Sub-Divisional Hospital",
        address = "DD Block, Sector I, Salt Lake, Kolkata 700064",
        lat = 22.5872,
        lng = 88.4115,
        phone = "033 2337 1318",
        emergencyHelpline = "033 2337 1318",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Casualty & Inpatient Wards",
        bedAvailability = 25,
        totalBeds = 150,
        icuAvailability = 4,
        traumaLevel = "Sub-Divisional Emergency Center"
      ),
      FacilitySeed(
        name = "AMRI Hospitals — Salt Lake",
        address = "16, 17, JC Block, Broadway Rd, Sector III, Salt Lake 700098",
        lat = 22.5712,
        lng = 88.4120,
        phone = "033 6606 3800",
        emergencyHelpline = "033 6606 3800",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Emergency Center • Cardiac ICU",
        bedAvailability = 38,
        totalBeds = 220,
        icuAvailability = 7,
        traumaLevel = "Level 1 Emergency Center"
      ),
      FacilitySeed(
        name = "Apollo Multispeciality Hospitals",
        address = "58, Canal Circular Rd, Kadapara, Phoolbagan, Kolkata 700054",
        lat = 22.5685,
        lng = 88.4012,
        phone = "033 2320 3040",
        emergencyHelpline = "033 2320 2122",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "JCI-Accredited Comprehensive Cardiac & Emergency",
        bedAvailability = 64,
        totalBeds = 510,
        icuAvailability = 12,
        traumaLevel = "Comprehensive Cardiac & Trauma Center"
      ),
      FacilitySeed(
        name = "ILS Hospitals — Salt Lake",
        address = "DD-6, Sector 1, Salt Lake, Kolkata 700064",
        lat = 22.5890,
        lng = 88.4200,
        phone = "033 4031 5000",
        emergencyHelpline = "033 4031 5050",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 ER • Trauma & Acute Care",
        bedAvailability = 22,
        totalBeds = 135,
        icuAvailability = 4,
        traumaLevel = "Trauma & Acute Emergency Care"
      ),
      FacilitySeed(
        name = "Tata Medical Center — New Town",
        address = "14, Major Arterial Road (E-W), Action Area I, New Town 700160",
        lat = 22.5855,
        lng = 88.4820,
        phone = "033 6605 7000",
        emergencyHelpline = "033 6605 7222",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Specialized Critical Care & Emergency Oncology",
        bedAvailability = 30,
        totalBeds = 437,
        icuAvailability = 6,
        traumaLevel = "Specialized Critical Care"
      ),
      FacilitySeed(
        name = "Chittaranjan National Cancer Institute (CNCI) — New Town",
        address = "Street No. 299, DJ Block, Action Area I, New Town 700156",
        lat = 22.5740,
        lng = 88.4680,
        phone = "033 2324 5015",
        emergencyHelpline = "033 2324 5000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency & Acute Support ICU",
        bedAvailability = 40,
        totalBeds = 460,
        icuAvailability = 8,
        traumaLevel = "Emergency & Critical Support Hub"
      ),
      FacilitySeed(
        name = "SSKM Hospital & IPGMER",
        address = "244, AJC Bose Rd, Bhowanipore, Kolkata 700020",
        lat = 22.5390,
        lng = 88.3440,
        phone = "033 2204 1100",
        emergencyHelpline = "033 2223 1589",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Apex Level 1 Govt Trauma Center",
        bedAvailability = 110,
        totalBeds = 2100,
        icuAvailability = 18,
        traumaLevel = "Apex Level 1 Government Trauma Center"
      ),
      FacilitySeed(
        name = "Fortis Hospital — Anandapur",
        address = "730, Anandapur, E.M. Bypass Road, Kolkata 700107",
        lat = 22.5186,
        lng = 88.3995,
        phone = "033 6628 4444",
        emergencyHelpline = "033 6628 4444",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Trauma Center • Comprehensive Cardiac & Neuro",
        bedAvailability = 45,
        totalBeds = 400,
        icuAvailability = 9,
        traumaLevel = "Level 1 Trauma Center"
      ),
      FacilitySeed(
        name = "Medica Superspecialty Hospital",
        address = "127, Mukundapur, E.M. Bypass, Kolkata 700099",
        lat = 22.4988,
        lng = 88.3970,
        phone = "033 6652 0000",
        emergencyHelpline = "033 6652 0000",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "Level 1 Trauma & Comprehensive Cardiac Center",
        bedAvailability = 52,
        totalBeds = 500,
        icuAvailability = 14,
        traumaLevel = "Level 1 Trauma & Cardiac Emergency Center"
      ),
      FacilitySeed(
        name = "Ruby General Hospital",
        address = "Kasba Golpark, E.M. Bypass, Kolkata 700107",
        lat = 22.5133,
        lng = 88.4011,
        phone = "033 3987 1800",
        emergencyHelpline = "033 3987 1800",
        category = EmergencyMapCategory.HOSPITALS,
        icon = Icons.Default.LocalHospital,
        subDetail = "24/7 Emergency Surgery & Trauma ICU",
        bedAvailability = 28,
        totalBeds = 278,
        icuAvailability = 5,
        traumaLevel = "Emergency & Trauma Care"
      ),

      // ==============================================================
      // 5. POLICE STATIONS ACROSS BARASAT, BARRACKPORE & KOLKATA
      // ==============================================================
      FacilitySeed(
        name = "Barasat Police Station",
        address = "Jessore Road, K.N.C. Road Crossing, Barasat, Kolkata 700124",
        lat = 22.7235,
        lng = 88.4805,
        phone = "033 2552 3221",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Barasat Police District Headquarters • 24/7 Emergency Command"
      ),
      FacilitySeed(
        name = "Barrackpore Police Station",
        address = "River Road, Cantonment, Barrackpore, Kolkata 700120",
        lat = 22.7635,
        lng = 88.3610,
        phone = "033 2592 0064",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Barrackpore Police Commissionerate • 24/7 Patrol Command"
      ),
      FacilitySeed(
        name = "Titagarh Police Station",
        address = "B.T. Road, Titagarh, Kolkata 700119",
        lat = 22.7410,
        lng = 88.3710,
        phone = "033 2501 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Titagarh & Barrackpore Corridor Rapid Police Response"
      ),
      FacilitySeed(
        name = "Khardah Police Station",
        address = "B.T. Road, Khardah, Kolkata 700117",
        lat = 22.7150,
        lng = 88.3750,
        phone = "033 2553 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Khardah & Rahara Area Emergency Police Patrol"
      ),
      FacilitySeed(
        name = "Ghola Police Station",
        address = "Sodepur-Barasat Road, Ghola, Kolkata 700111",
        lat = 22.7050,
        lng = 88.4020,
        phone = "033 2565 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Sodepur-Barasat Road Emergency Patrol Hub"
      ),
      FacilitySeed(
        name = "New Barrackpore Police Station",
        address = "New Barrackpore Main Road, North 24 Parganas 700131",
        lat = 22.6980,
        lng = 88.4520,
        phone = "033 2567 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "New Barrackpore 24/7 Emergency Police Station"
      ),
      FacilitySeed(
        name = "Madhyamgram Police Station",
        address = "Jessore Road, Madhyamgram Chowmatha 700129",
        lat = 22.7020,
        lng = 88.4680,
        phone = "033 2538 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Madhyamgram Police Command & Highway Patrol"
      ),
      FacilitySeed(
        name = "Nimta Police Station",
        address = "M.B. Road, Nimta, North Dumdum, Kolkata 700049",
        lat = 22.6610,
        lng = 88.4120,
        phone = "033 2539 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Nimta & Birati Area Rapid Police Patrol"
      ),
      FacilitySeed(
        name = "Airport Police Station",
        address = "Gate 1, NSCBI Airport, Jessore Road, Kolkata 700052",
        lat = 22.6480,
        lng = 88.4420,
        phone = "033 2511 8100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "NSCBI Airport Emergency Police Command"
      ),
      FacilitySeed(
        name = "Dum Dum Police Station",
        address = "Dum Dum Cantonment, Kolkata 700065",
        lat = 22.6260,
        lng = 88.4210,
        phone = "033 2549 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Dum Dum Police Command & Rapid Patrol"
      ),
      FacilitySeed(
        name = "Belgharia Police Station",
        address = "Feeder Road, Belgharia, Kolkata 700056",
        lat = 22.6470,
        lng = 88.3840,
        phone = "033 2564 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Belgharia & Kamarhati Rapid Police Dispatch"
      ),
      FacilitySeed(
        name = "Baguiati Police Station",
        address = "VIP Road, Raghunathpur, Baguiati, Kolkata 700059",
        lat = 22.6175,
        lng = 88.4310,
        phone = "033 2570 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "VIP Road Emergency Police Patrol"
      ),
      FacilitySeed(
        name = "Lake Town Police Station",
        address = "Block A, Lake Town, South Dumdum, Kolkata 700089",
        lat = 22.6030,
        lng = 88.4020,
        phone = "033 2521 1000",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Lake Town & VIP Corridor Emergency Unit"
      ),
      FacilitySeed(
        name = "Electronic Complex Police Station (Sector V)",
        address = "DP Block, Sector V, Bidhannagar, Kolkata 700091",
        lat = 22.5810,
        lng = 88.4320,
        phone = "033 2357 5800",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "24/7 Police Outpost • Sector V Rapid Patrol"
      ),
      FacilitySeed(
        name = "Bidhannagar North Police Station",
        address = "DF Block, Sector I, Salt Lake, Kolkata 700064",
        lat = 22.5925,
        lng = 88.4140,
        phone = "033 2337 1319",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "Salt Lake Sector I & II Emergency Command"
      ),
      FacilitySeed(
        name = "New Town Police Station",
        address = "Major Arterial Road, Action Area I, New Town 700156",
        lat = 22.5880,
        lng = 88.4610,
        phone = "033 2324 0100",
        category = EmergencyMapCategory.POLICE,
        icon = Icons.Default.LocalPolice,
        subDetail = "New Town Action Area I Police Command"
      ),

      // ==============================================================
      // 6. PHARMACIES
      // ==============================================================
      FacilitySeed(
        name = "Apollo Pharmacy — Barasat",
        address = "Jessore Road, Champadali More, Barasat 700124",
        lat = 22.7210,
        lng = 88.4780,
        phone = "+91 98305 11223",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24-Hour Medicine Supply & Oxygen Cylinder Delivery"
      ),
      FacilitySeed(
        name = "Frank Ross Pharmacy — Barrackpore",
        address = "S. N. Banerjee Road, Barrackpore, Kolkata 700120",
        lat = 22.7620,
        lng = 88.3680,
        phone = "+91 98311 33445",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Life-Saving Prescriptions & Critical Care Supplies"
      ),
      FacilitySeed(
        name = "MedPlus 24x7 Chemist — Sodepur",
        address = "Sodepur Station Road, Near B.T. Road, Kolkata 700110",
        lat = 22.7000,
        lng = 88.3820,
        phone = "+91 98302 44556",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24/7 Emergency Prescriptions & Nebulizer Kits"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Madhyamgram",
        address = "Jessore Road, Madhyamgram Chowmatha 700129",
        lat = 22.7030,
        lng = 88.4660,
        phone = "+91 98307 55667",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Essential & Cardiac Emergency Meds"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Lake Town",
        address = "Block A, Lake Town, Kolkata 700089",
        lat = 22.6040,
        lng = 88.4035,
        phone = "+91 98302 33441",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24/7 Medicine Delivery & First Aid Kits"
      ),
      FacilitySeed(
        name = "Frank Ross Pharmacy — Baguiati",
        address = "VIP Road, Baguiati, Kolkata 700059",
        lat = 22.6160,
        lng = 88.4300,
        phone = "+91 98314 55662",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Life-Saving Prescriptions & First Aid"
      ),
      FacilitySeed(
        name = "MedPlus Chemist — Dum Dum",
        address = "Dum Dum Road, Near Metro, Kolkata 700074",
        lat = 22.6240,
        lng = 88.4190,
        phone = "+91 98308 77883",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "24-Hour Emergency Prescription Supply"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — Sector V",
        address = "Godrej Genesis, Sector V, Salt Lake 700091",
        lat = 22.5820,
        lng = 88.4350,
        phone = "+91 98300 22114",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Emergency Meds & Oxygen Cylinders"
      ),
      FacilitySeed(
        name = "Apollo Pharmacy — New Town",
        address = "City Centre 2, New Town, Kolkata 700136",
        lat = 22.6220,
        lng = 88.4550,
        phone = "+91 98309 99001",
        category = EmergencyMapCategory.PHARMACIES,
        icon = Icons.Default.LocalPharmacy,
        subDetail = "Emergency Pediatric & Trauma Supplies"
      ),
    )
  } else {
    emptyList()
  }

  return seeds.mapIndexed { index, seed ->
    val distM = computeDistanceMeters(userLat, userLng, seed.lat, seed.lng)
    EmergencyMapFacility(
      id = "fac_${seed.category.name.lowercase()}_$index",
      name = seed.name,
      address = seed.address,
      category = seed.category,
      distance = formatDistance(distM),
      distanceKm = distM / 1000.0,
      status = if (seed.category == EmergencyMapCategory.POLICE) "24/7 Police Station" else "Open 24 hours",
      subDetail = seed.subDetail,
      bedAvailability = seed.bedAvailability,
      totalBeds = seed.totalBeds,
      icuAvailability = seed.icuAvailability,
      traumaLevel = seed.traumaLevel,
      emergencyHelpline = seed.emergencyHelpline.ifBlank { seed.phone },
      phone = seed.phone,
      icon = seed.icon,
      lat = seed.lat,
      lng = seed.lng,
    )
  }
}
