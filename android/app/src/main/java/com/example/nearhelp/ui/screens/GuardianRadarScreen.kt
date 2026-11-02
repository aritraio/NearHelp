package com.example.nearhelp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.ui.design.*
import kotlinx.coroutines.launch

@Composable
fun GuardianRadarScreen(onExit: () -> Unit, onNavigateToCrisis: (String) -> Unit, onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit, onVoiceSosClick: () -> Unit, modifier: Modifier = Modifier,
    localityName: String? = null, safetyIndex: Int? = null, coordinatesText: String? = null, isAnonymous: Boolean = false) {
    val (location, requestLocation) = rememberCareLocation()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    CarePage("Medical guardian", onExit, modifier) {
        CareMap(location)
        CareHeading("Keep your contacts close.", "Send a check-in or request medical help.")
        status?.let { CareNote("Check-in", it) }
        if (!location.hasPermission) CareButton("Allow location access", requestLocation, icon = Icons.Outlined.MyLocation)
        CareButton("Send a check-in", {
            busy = true
            scope.launch {
                val result = NearHelpApplication.instance.userRepository.listEmergencyContacts()
                val contacts = result.getOrNull().orEmpty()
                if (contacts.isEmpty()) status = result.exceptionOrNull()?.message ?: "Add an emergency contact in your profile first."
                else {
                    val numbers = contacts.joinToString(";") { it.phone }
                    val body = "I’m checking in with NearHelp. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$numbers")).putExtra("sms_body", body)) }
                        .onSuccess { status = "Your check-in message is ready to send." }
                        .onFailure { status = "Messaging is unavailable on this device." }
                }
                busy = false
            }
        }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.CheckCircle, enabled = !busy && location.hasFix)
        CareButton("SOS · Get medical help", { onNavigateToCrisis("medical") }, style = CareButtonStyle.SOS, icon = Icons.Outlined.MonitorHeart)
        CareButton("Emergency contacts", onNavigateToProfile, icon = Icons.Outlined.Contacts)
        CareButton("Open nearby facilities", onNavigateToMap, icon = Icons.Outlined.LocationOn)
        CareButton("Exit guardian mode", onExit, style = CareButtonStyle.QUIET)
    }
}
