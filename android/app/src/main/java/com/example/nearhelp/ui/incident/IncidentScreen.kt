package com.example.nearhelp.ui.incident

import android.Manifest
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.createSavedStateHandle
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.notifications.PushRegistration
import com.example.nearhelp.ui.assistant.AiCrisisAssistantViewModel
import com.example.nearhelp.ui.assistant.CareAiConversation
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.map.MapPinItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun IncidentScreen(initialId: String = "", createMode: Boolean = false, onBack: () -> Unit,
                   viewModel: IncidentViewModel = viewModel(), prompt: MedicalSosPromptViewModel = viewModel { MedicalSosPromptViewModel(createSavedStateHandle()) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val medical by prompt.state.collectAsStateWithLifecycle()
    val (location, requestLocation) = rememberCareLocation()
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    var draft by rememberSaveable { mutableStateOf("") }
    var panel by rememberSaveable { mutableStateOf("tracking") }
    var available by rememberSaveable { mutableStateOf(false) }
    var confirmFinish by rememberSaveable { mutableStateOf<String?>(null) }
    var tick by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val ai: AiCrisisAssistantViewModel = viewModel { AiCrisisAssistantViewModel(NearHelpApplication.instance.aiAgentRepository) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { PushRegistration.sync() }
    val incident = state.incident
    val creating = createMode && viewModel.activeId == null
    val ended = incident?.status in listOf("RESOLVED", "CANCELLED")
    val owner = incident != null && incident.broadcasterId == viewModel.userId
    val responses = incident?.responses.orEmpty().filter { it.status in listOf("ACCEPTED", "EN_ROUTE", "ARRIVED", "ON_SCENE", "HANDOVER_108") }
    val responding = responses.any { it.responderId == viewModel.userId }
    val positions = state.snapshot?.responders.orEmpty().filter { position -> responses.any { it.responderId == position.responderId } }
    val closest = positions.minByOrNull { it.distanceMeters }
    val arrived = responses.any { it.status in listOf("ARRIVED", "ON_SCENE") } || positions.any { it.status in listOf("ARRIVED", "ON_SCENE") }
    val stale = responses.isNotEmpty() && (state.connection != ConnectionStatus.CONNECTED || state.lastTrackingUpdateElapsed?.let { tick - it > 30_000 } != false)
    val center = incident?.let { LatLng(it.latitude, it.longitude) }
    val pins = positions.map { MapPinItem(it.responderId, it.responderName, if (stale) "Last known location" else it.status.replace('_', ' '), LatLng(it.latitude, it.longitude), BitmapDescriptorFactory.HUE_GREEN) }
    val safeBack = { if (!state.busy) { if (panel != "tracking") panel = "tracking" else { prompt.cancel(); onBack() } } }

    LaunchedEffect(Unit) {
        if (!creating || viewModel.activeId != null) viewModel.start(initialId) else prompt.start()
        requestLocation()
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        PushRegistration.sync()
    }
    LaunchedEffect(medical.description, medical.claimed, location.hasFix, location.fixElapsedMillis, lifecycle) {
        if (creating && medical.description != null && !medical.claimed && lifecycle.isAtLeast(Lifecycle.State.RESUMED)
            && location.hasFix && SystemClock.elapsedRealtime() - location.fixElapsedMillis < 120_000) {
            prompt.claim()?.let { description ->
                val savedLandmark = context.getSharedPreferences("location_label", android.content.Context.MODE_PRIVATE).getString("landmark", "").orEmpty()
                viewModel.create(location, "medical", description, savedLandmark.ifBlank { location.localityName })
            }
        }
    }
    LaunchedEffect(location.latitude, location.longitude, location.fixElapsedMillis, available, state.role, state.connection) { if (location.hasFix) viewModel.updateLocation(location, available) }
    LaunchedEffect(available) { while (available) { if (location.hasFix) viewModel.updateLocation(location, true); delay(60_000) } }
    LaunchedEffect(incident?.id) { incident?.let { ai.initialize(sessionId = it.id) } }
    LaunchedEffect(Unit) { while (true) { delay(1_000); tick = SystemClock.elapsedRealtime() } }
    BackHandler { safeBack() }

    CarePage(if (creating) null else when (panel) { "ai" -> "AI help while waiting"; "messages" -> "Incident messages"; "details" -> "Add medical details"; else -> if (incident == null) "Medical requests" else "Your medical SOS" }, safeBack, Modifier.navigationBarsPadding()) {
        if (creating || incident != null) {
            CareMap(location, center = center, pins = pins,
                route = if (stale) emptyList() else state.routePoints.map { LatLng(it.lat, it.lng) },
                height = if (panel == "ai" || panel == "messages") 160.dp else 270.dp,
                status = if (stale) "Last known responder location" else if (center != null) "Medical SOS location" else null)
        }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { error ->
            CareNote("We couldn’t complete that action", error, Icons.Outlined.WifiOff, error = true)
            CareButton(if (creating) "Retry sending medical SOS" else "Retry connection", {
                if (creating) viewModel.create(location, "medical", medical.description ?: GENERIC_MEDICAL_SOS, location.localityName)
                else if (viewModel.activeId != null) viewModel.reconnect() else viewModel.browse()
            }, enabled = !state.busy && (!creating || location.hasFix))
        }
        if (creating) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.background, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (medical.description == null) {
                        CareHeading("What’s the medical emergency?", "Tap a symptom or condition to send it now.", compact = true)
                        CareNote("${ceil(medical.remainingMillis / 1000.0).toInt()}s until a general medical SOS", "Your SOS sends even without details.", Icons.Outlined.Timer, error = true)
                        LinearProgressIndicator(progress = { medical.remainingMillis / 4000f }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error, trackColor = MaterialTheme.colorScheme.errorContainer)
                        val options = listOf("Chest pain", "Breathing trouble", "Injury / bleeding", "Fainting", "Seizure", "Other / not sure")
                        options.chunked(2).forEach { pair -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { label -> OutlinedButton(onClick = { prompt.submit(if (label == "Other / not sure") "" else label) }, Modifier.weight(1f).heightIn(min = 58.dp), shape = RoundedCornerShape(11.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text(label, style = MaterialTheme.typography.bodyMedium) } }
                        } }
                        CareField("Or describe what happened", draft, { draft = it.take(180) }, hint = "Symptoms or a known condition")
                        CareButton("Send medical SOS now", { prompt.submit(draft) }, style = CareButtonStyle.SOS, icon = Icons.Outlined.Sos)
                    } else {
                        CareHeading(if (state.busy) "Sending medical SOS…" else if (!location.hasFix) "Waiting for your location" else "Medical SOS pending")
                        CareMuted("Your request: ${medical.description}")
                        if (!location.hasFix) { CareNote("Location access needed", "Turn on location and allow GPS access to send your medical SOS.", Icons.Outlined.LocationOff); CareButton("Allow location access", requestLocation, icon = Icons.Outlined.MyLocation) }
                        if (medical.claimed && !state.busy && state.error == null && location.hasFix) {
                            CareMuted("Sending has not been confirmed. Retry to check your request.")
                            CareButton("Retry medical SOS", { viewModel.create(location, "medical", medical.description ?: GENERIC_MEDICAL_SOS, location.localityName) }, style = CareButtonStyle.SOS)
                        }
                        EmergencyCallButton()
                    }
                    CareButton("Cancel SOS", safeBack, style = CareButtonStyle.QUIET, enabled = !state.busy)
                }
            }
        } else if (incident == null) {
            CareMap(location)
            CareCheck("Available to help nearby", available, { available = it })
            CareMuted("Share your location to receive nearby medical alerts.")
            CareHeading("Medical requests")
            val feed = state.feed.filter { it.crisisType.equals("medical", true) }
            if (feed.isEmpty()) CareNote("No active medical requests", "The feed updates automatically.", Icons.Outlined.CheckCircle)
            feed.forEach { event -> CareRow("Medical emergency", listOfNotNull(event.address, event.status.replace('_', ' ')).joinToString(" · "), Icons.Outlined.MonitorHeart) { viewModel.open(event.id) } }
            EmergencyCallButton()
        } else {
            when (panel) {
                "ai" -> {
                    CareNote(if (arrived) "Your responder has arrived" else if (responses.isEmpty()) "Your medical SOS is active" else "Your responder is on the way",
                        if (responses.isEmpty()) "Waiting for someone to accept" else if (stale) "Location updates are delayed" else closest?.let { "${it.etaFormatted} · ${it.distanceFormatted} away" }, Icons.Outlined.Navigation)
                    CareButton("View my medical SOS", { panel = "tracking" }, style = CareButtonStyle.QUIET)
                    CareAiConversation(ai)
                }
                "messages", "details" -> {
                    CareHeading(if (panel == "details") "Add medical details" else "Incident messages")
                    if (panel == "details") CareMuted("Your SOS is already active. These details will be shared with its participants.")
                    incident.timeline.forEach { event -> CareNote(event.actorName ?: "NearHelp", if (event.eventType == "chat_message") event.details["text"]?.toString() else event.eventType.replace('_', ' '), Icons.Outlined.ChatBubbleOutline) }
                    if (!ended && (owner || responding)) {
                        CareField(if (panel == "details") "Symptoms or known condition" else "Message participants", draft, { draft = it.take(1000) }, multiline = true)
                        CareButton(if (panel == "details") "Update my medical SOS" else "Send message", {
                            if (viewModel.send(if (panel == "details") "Medical details: $draft" else draft)) draft = ""
                        }, style = CareButtonStyle.PRIMARY, enabled = draft.isNotBlank() && state.connection == ConnectionStatus.CONNECTED)
                    }
                }
                else -> {
                    if (ended) {
                        CareHeading(if (incident.status == "RESOLVED") "Medical request resolved" else "Medical SOS cancelled")
                        CareMuted(incident.address ?: "")
                        CareButton("Back to Home", onBack, style = CareButtonStyle.PRIMARY)
                    } else {
                        CareResponseProgress(if (arrived) 3 else if (responses.isNotEmpty()) 2 else 0)
                        CareHeading(if (arrived) "Your responder has arrived" else if (responses.isEmpty()) if (owner) "Medical SOS sent" else "Medical emergency" else if (stale) "ETA updating…" else closest?.etaFormatted?.let { "Arriving in $it" } ?: "Responder accepted",
                            if (responses.isEmpty()) "Waiting for a nearby responder to accept." else incident.address)
                        if (!owner && !responding) CareButton("I’m responding", { viewModel.respond(location) }, style = CareButtonStyle.PRIMARY, enabled = !state.busy, icon = Icons.Outlined.VolunteerActivism)
                        if (owner || responding) CareButton("Ask AI for help", { panel = "ai" }, icon = Icons.Outlined.SmartToy, subtitle = "Guidance while you wait for a responder")
                        if (!incident.description.isNullOrBlank()) CareNote("Medical SOS details", incident.description, Icons.Outlined.Description)
                        if (stale) CareNote("Location update delayed", "Showing the last known responder position. Arrival estimates are unavailable.", Icons.Outlined.WifiOff)
                        responses.forEach { response ->
                            val position = positions.firstOrNull { it.responderId == response.responderId }
                            CareRow(response.responderName, if (position == null || stale) "${response.status.replace('_', ' ')} · waiting for GPS" else "${position.status.replace('_', ' ')} · ${position.distanceFormatted} · ${position.etaFormatted}", Icons.Outlined.PersonOutline)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val phone = position?.phone
                                if (!phone.isNullOrBlank()) CareButton("Call", { dial(context, phone) }, Modifier.weight(1f), icon = Icons.Outlined.Phone)
                                CareButton("Message", { panel = "messages" }, Modifier.weight(1f), icon = Icons.Outlined.ChatBubbleOutline)
                            }
                        }
                        if (responding) {
                            CareNote("Sharing your location", "The requester can follow your arrival while this screen is open.", Icons.Outlined.MyLocation)
                            CareButton("Open directions", { center?.let { directions(context, it, incident.address.orEmpty()) } }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Navigation)
                            if (!arrived) CareButton("I have arrived", { viewModel.markArrived() }, enabled = state.connection == ConnectionStatus.CONNECTED)
                        }
                        if (owner) CareButton("Add medical details", { draft = ""; panel = "details" }, icon = Icons.Outlined.Edit)
                        CareButton("View incident messages", { panel = "messages" }, icon = Icons.Outlined.ChatBubbleOutline)
                        EmergencyCallButton()
                        if (owner) {
                            CareButton("Mark incident resolved", { confirmFinish = "resolve" }, style = CareButtonStyle.PRIMARY, enabled = !state.busy)
                            CareButton("Cancel SOS", { confirmFinish = "cancel" }, style = CareButtonStyle.QUIET, enabled = !state.busy)
                        }
                    }
                }
            }
        }
    }
    confirmFinish?.let { action -> AlertDialog(onDismissRequest = { confirmFinish = null },
        title = { Text(if (action == "cancel") "Cancel this medical SOS?" else "Resolve this medical request?") },
        text = { Text("This updates the incident for everyone participating.") },
        confirmButton = { TextButton(onClick = { confirmFinish = null; viewModel.finish(action == "cancel") }) { Text(if (action == "cancel") "Yes, cancel SOS" else "Yes, mark resolved") } },
        dismissButton = { TextButton(onClick = { confirmFinish = null }) { Text("Keep SOS active") } }) }
}

@Composable
private fun CareResponseProgress(stage: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("SOS sent", "Accepted", "On the way", "Arrived").forEachIndexed { index, label ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(4.dp).background(if (index <= stage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)))
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (index <= stage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
