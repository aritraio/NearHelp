package com.example.nearhelp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.ui.auth.AuthViewModel
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToResponderProfile: () -> Unit = onNavigateToProfile,
    onNavigateToHistory: () -> Unit = onNavigateToProfile,
    onNavigateToMap: () -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onCreateSos: () -> Unit = onNavigateToTracking,
    onNavigateToAssistant: () -> Unit = {},
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    onOverlayStateChanged: (Boolean) -> Unit = {},
) {
    val (location, requestLocation) = rememberCareLocation()
    var editLocation by rememberSaveable { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("location_label", android.content.Context.MODE_PRIVATE) }
    var landmark by rememberSaveable { mutableStateOf(preferences.getString("landmark", "").orEmpty()) }
    var draft by rememberSaveable { mutableStateOf(landmark) }
    // Read again whenever Home is re-entered after an incident is created or closed.
    val activeIncident = NearHelpApplication.instance.tokenStorage.getActiveIncidentId()
    LaunchedEffect(editLocation) { onOverlayStateChanged(editLocation) }
    Column(modifier.fillMaxSize()) {
        CarePage(if (editLocation) "Your location" else null, if (editLocation) ({ editLocation = false }) else null, Modifier.weight(1f)) {
            if (!editLocation) { CareBrand(); CareHeading("Medical help, nearby.", "Your location stays in view.") }
            CareMap(location)
            if (editLocation) {
                CareHeading("Confirm your location")
                CareField("Address or landmark", draft, { draft = it }, hint = "Add a nearby landmark")
                CareButton("Use current GPS location", { draft = ""; requestLocation() }, icon = Icons.Outlined.MyLocation)
                CareMuted("Add a nearby landmark so responders can find you.")
                CareButton("Save location", { landmark = draft.trim(); preferences.edit().putString("landmark", landmark).apply(); editLocation = false }, style = CareButtonStyle.PRIMARY)
                CareButton("Cancel", { editLocation = false }, style = CareButtonStyle.QUIET)
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { CareMuted(landmark.ifBlank { location.localityName }); CareMuted("GPS location is shared with your SOS") }
                    TextButton(onClick = { draft = landmark; editLocation = true }) { Text("Edit") }
                }
                if (!location.hasPermission) CareButton("Allow location access", requestLocation, icon = Icons.Outlined.MyLocation)
                if (activeIncident != null) CareButton("Track your medical SOS", onNavigateToTracking, style = CareButtonStyle.PRIMARY, subtitle = "View acceptance and live arrival status", icon = Icons.Outlined.Navigation)
                else CareButton("SOS · Get medical help", onCreateSos, style = CareButtonStyle.SOS, subtitle = "Optional details · sends in 4 seconds")
                Column {
                    CareRow("Nearby medical requests", "Help someone in your community", Icons.Outlined.Groups, onNavigateToTracking)
                    CareRow("First-aid guidance", "Find a topic or ask the assistant", Icons.AutoMirrored.Outlined.MenuBook, onNavigateToAssistant)
                    CareRow("Become a responder", "Add your qualifications", Icons.Outlined.VerifiedUser, onNavigateToResponderProfile)
                }
            }
        }
        if (showBottomBar) VictimBottomNavBar(VictimNavTab.HOME, { when (it) { VictimNavTab.HOME -> {}; VictimNavTab.CHAT -> onNavigateToAssistant(); VictimNavTab.MAP -> onNavigateToMap(); VictimNavTab.PROFILE -> onNavigateToProfile() } })
    }
}
