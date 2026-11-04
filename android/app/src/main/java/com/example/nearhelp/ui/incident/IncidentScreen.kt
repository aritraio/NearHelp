package com.example.nearhelp.ui.incident

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.createSavedStateHandle
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.data.places.GeoPoint
import com.example.nearhelp.data.places.usableLocation
import com.example.nearhelp.i18n.LocalizedText as Text
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
    viewModel: IncidentViewModel = viewModel { IncidentViewModel(savedState = createSavedStateHandle()) },
    prompt: MedicalSosPromptViewModel = viewModel { MedicalSosPromptViewModel(createSavedStateHandle()) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val medical by prompt.state.collectAsStateWithLifecycle()
    val (location, requestLocation) = rememberCareLocation()
    val context = LocalContext.current
    val language = LocalConfiguration.current.locales[0].language
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    var draft by rememberSaveable { mutableStateOf("") }
    var panel by rememberSaveable { mutableStateOf("tracking") }
    var available by rememberSaveable { mutableStateOf(false) }
    var confirmFinish by rememberSaveable { mutableStateOf<String?>(null) }
    var requestedLocation by rememberSaveable { mutableStateOf(false) }
    var requestedNotifications by rememberSaveable { mutableStateOf(false) }
    var tick by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val aiSession = rememberSaveable { java.util.UUID.randomUUID().toString() }
    val ai: AiCrisisAssistantViewModel = viewModel { AiCrisisAssistantViewModel(NearHelpApplication.instance.aiAgentRepository) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { PushRegistration.sync() }
    val incident = state.incident
    val currentElapsed = maxOf(tick, SystemClock.elapsedRealtime())
    val presentation = presentIncident(state, currentElapsed, System.currentTimeMillis())
    val ended = presentation.phase in listOf(RescuePhase.RESOLVED, RescuePhase.CANCELLED)
    val owner = incident != null && incident.broadcasterId == viewModel.userId
    val myResponse = presentation.responders.firstOrNull { it.id == viewModel.userId }
    val pending = incident == null && state.submission != SosSubmission.NONE
    val showPrompt = createMode && state.submission == SosSubmission.CHOOSING && medical.description == null && !medical.cancelled
    val locationFresh = usableLocation(GeoPoint(location.latitude, location.longitude), location.hasFix, location.hasPermission,
        location.fixElapsedMillis, currentElapsed)
    val center = incident?.let { LatLng(it.latitude, it.longitude) }
    val pins = if (ended) emptyList() else presentation.responders.mapNotNull { responder -> responder.position?.let {
        MapPinItem(responder.id, responder.name, if (responder.fresh) responder.eta ?: responder.status else "Last known location",
            LatLng(it.latitude, it.longitude), BitmapDescriptorFactory.HUE_GREEN)
    } }
    val safeBack = {
        if (state.submission in listOf(SosSubmission.CHOOSING, SosSubmission.WAITING_FOR_LOCATION)) prompt.cancel()
        if (panel != "tracking") panel = "tracking" else onBack()
    }
    fun cancelSos() {
        if (state.submission in listOf(SosSubmission.CHOOSING, SosSubmission.WAITING_FOR_LOCATION)) { prompt.cancel(); onBack() }
        else confirmFinish = "cancel"
    }

    LaunchedEffect(Unit) { if (createMode) viewModel.beginCreate() else viewModel.start(initialId) }
    LaunchedEffect(showPrompt, lifecycle) { if (showPrompt && lifecycle.isAtLeast(Lifecycle.State.RESUMED)) prompt.start() }
    LaunchedEffect(medical.description, state.submission, location, locationFresh) {
        val description = medical.description
        if (description != null && !medical.cancelled && state.submission in listOf(SosSubmission.CHOOSING, SosSubmission.WAITING_FOR_LOCATION)) {
            viewModel.awaitLocation(description)
            if (locationFresh) {
                val landmark = context.getSharedPreferences("location_label", android.content.Context.MODE_PRIVATE).getString("landmark", "").orEmpty()
                viewModel.create(location, "medical", description, landmark.ifBlank { location.localityName })
            }
        }
    }
    // Permission dialogs must not cover or consume the five-second emergency question.
    LaunchedEffect(showPrompt, state.submission, state.incidentId) {
        val mayRequest = !createMode || state.incidentId != null || state.submission in listOf(SosSubmission.WAITING_FOR_LOCATION, SosSubmission.SENDING, SosSubmission.FAILED)
        if (mayRequest && !showPrompt && !requestedLocation) {
            requestedLocation = true; requestLocation()
        }
        if (state.incidentId != null && !requestedNotifications) {
            requestedNotifications = true
            if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            PushRegistration.sync()
        }
    }
    LaunchedEffect(location, available, state.role, state.incident?.responses, state.connection) { viewModel.updateLocation(location, available) }
    // One conversation survives the transition from sending to confirmed tracking.
    LaunchedEffect(aiSession) { ai.initialize(conditionId = "general_emergency", sessionId = aiSession) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); tick = SystemClock.elapsedRealtime() } }
    BackHandler { safeBack() }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding().imePadding()) {
        CareTopBar(when (panel) { "messages" -> "Incident messages"; "details" -> "Add medical details"; else -> if (pending || incident != null) "Your medical SOS" else if (state.incidentId != null) "Loading your medical SOS…" else "Medical requests" }, safeBack)
        // This map is outside the scrolling conversation: location stays visible while asking for help.
        CareMap(location, center = center, pins = pins, modifier = Modifier.padding(horizontal = 20.dp), height = 160.dp,
            route = if (!ended && presentation.primary?.fresh == true) state.routePoints.map { LatLng(it.lat, it.lng) } else emptyList(),
            status = if (center != null) "Medical SOS location" else null)
        CarePage(modifier = Modifier.weight(1f)) {
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { error ->
                CareNote(if (state.cancellationPending) "Cancellation not confirmed" else "We couldn’t complete that action", error, Icons.Outlined.WifiOff, true)
                CareButton(if (state.cancellationPending) "Retry cancellation" else if (state.submission == SosSubmission.FAILED) "Retry sending medical SOS" else "Refresh status", {
                    when { state.cancellationPending -> viewModel.finish(true); state.submission == SosSubmission.FAILED -> viewModel.retryPending(); else -> viewModel.refreshIncident() }
                }, enabled = !state.busy)
            }
            if (ended) {
                CareHeading(if (presentation.phase == RescuePhase.RESOLVED) "Medical request resolved" else "Medical SOS cancelled")
                CareMuted(incident?.address.orEmpty())
                CareButton("Back to Home", onBack, style = CareButtonStyle.PRIMARY)
            } else if (state.incidentId != null && incident == null) {
                CareNote("Loading your medical SOS…", "Your request is saved. Waiting for its latest status.")
                if (state.syncError != null) CareButton("Retry connection", viewModel::reconnect, icon = Icons.Outlined.Refresh)
            } else if (pending) {
                CareHeading(when {
                    state.cancellationPending -> "Cancelling your SOS…"
                    state.submission == SosSubmission.SENDING -> "Sending medical SOS…"
                    state.submission == SosSubmission.FAILED -> "Sending not confirmed"
                    else -> "Waiting for your location"
                })
                if (state.submission == SosSubmission.WAITING_FOR_LOCATION) {
                    CareNote("Current location needed", "Turn on location and allow GPS access to send your medical SOS.", Icons.Outlined.MyLocation)
                    CareButton(if (location.hasPermission) "Refresh GPS location" else "Allow location access", requestLocation, icon = Icons.Outlined.MyLocation)
                    TextButton(onClick = { context.startActivity(Intent(if (location.hasPermission) Settings.ACTION_LOCATION_SOURCE_SETTINGS else Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        if (location.hasPermission) null else Uri.parse("package:${context.packageName}"))) }) { Text("Open location settings") }
                }
                if (state.submission == SosSubmission.FAILED) CareMuted("Your SOS may have reached the server. Retry uses the same request to avoid duplicates.")
                state.pendingDescription?.let { CareMuted("Your request: $it") }
                if (state.cancellationPending) CareMuted("We’ll confirm the cancellation with the server. Keep this request available until it is confirmed.")
                CareHeading("AI help while waiting", compact = true)
                CareAiConversation(ai, state.pendingDescription)
            } else if (incident == null) {
                CareCheck("Available to help nearby", available, { available = it })
                CareMuted("Share your location to receive nearby medical alerts.")
                if (!location.hasPermission) CareButton("Allow location access", requestLocation, icon = Icons.Outlined.MyLocation)
                state.syncError?.let { CareNote("Live updates unavailable", it, Icons.Outlined.WifiOff) }
                val feed = state.feed.filter { it.crisisType.equals("medical", true) }
                if (feed.isEmpty()) CareNote("No active medical requests", "The feed updates automatically.", Icons.Outlined.CheckCircle)
                feed.forEach { event -> CareRow("Medical emergency", listOfNotNull(event.address, event.status.replace('_', ' ')).joinToString(" · "), Icons.Outlined.MonitorHeart) { viewModel.open(event.id) } }
            } else {
                RescueStatus(presentation)
                incident.description?.takeIf(String::isNotBlank)?.let { CareMuted("Your request: $it") }
                state.syncError?.let { CareNote("Live updates unavailable", it, Icons.Outlined.WifiOff); CareButton("Retry connection", viewModel::reconnect) }
                if (state.cancellationPending) CareNote("Cancelling your SOS…", "Waiting for the server to confirm cancellation.")
                if (panel in listOf("messages", "details")) {
                    if (panel == "details") CareMuted("Your SOS is already active. These details will be shared with its participants.")
                    incident.timeline.forEach { event -> CareNote(event.actorName ?: "NearHelp", if (event.eventType == "chat_message") event.details["text"]?.toString() else event.eventType.replace('_', ' '), Icons.Outlined.ChatBubbleOutline) }
                    if (owner || myResponse != null) {
                        CareField(if (panel == "details") "Symptoms or known condition" else "Message participants", draft, { draft = it.take(1000) }, multiline = true)
                        CareButton(if (panel == "details") "Update my medical SOS" else "Send message", {
                            if (viewModel.send(if (panel == "details") "Medical details: $draft" else draft, language)) draft = ""
                        }, style = CareButtonStyle.PRIMARY, enabled = draft.isNotBlank() && state.connection == ConnectionStatus.CONNECTED)
                    }
                    CareButton("View my medical SOS", { panel = "tracking" }, style = CareButtonStyle.QUIET)
                } else {
                    if (owner && presentation.phase == RescuePhase.ARRIVED) CareButton("Mark incident resolved", { confirmFinish = "resolve" }, style = CareButtonStyle.PRIMARY, enabled = !state.busy && !state.cancellationPending)
                    presentation.responders.forEach { responder ->
                        CareRow(responder.name, when {
                            responder.status in listOf("ARRIVED", "ON_SCENE", "HANDOVER_108") -> "Arrived"
                            responder.eta != null -> "Estimated arrival: ${responder.eta}"
                            responder.position != null && !responder.fresh -> "Location updates are delayed"
                            else -> "Arrival estimate pending"
                        }, Icons.Outlined.PersonOutline)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            responder.position?.phone?.takeIf(String::isNotBlank)?.let { phone -> CareButton("Call", { dial(context, phone) }, Modifier.weight(1f), icon = Icons.Outlined.Phone) }
                            if (owner || myResponse != null) CareButton("Message", { panel = "messages" }, Modifier.weight(1f), icon = Icons.Outlined.ChatBubbleOutline)
                        }
                    }
                    if (!owner && myResponse == null) CareButton("I’m responding", { viewModel.respond(location) }, style = CareButtonStyle.PRIMARY, enabled = !state.busy, icon = Icons.Outlined.VolunteerActivism)
                    if (myResponse != null) {
                        CareNote("Sharing your location", "The requester can follow your arrival while this screen is open.", Icons.Outlined.MyLocation)
                        CareButton("Open directions", { center?.let { directions(context, it, incident.address.orEmpty()) } }, icon = Icons.Outlined.Navigation)
                        if (myResponse.status == "ACCEPTED") CareButton("I’m on the way", { viewModel.respond(location, "EN_ROUTE") }, style = CareButtonStyle.PRIMARY, enabled = !state.busy)
                        if (myResponse.status !in listOf("ARRIVED", "ON_SCENE", "HANDOVER_108")) CareButton("I have arrived", { viewModel.markArrived(location) }, enabled = !state.busy)
                    }
                    CareHeading("AI help while waiting", "Ask for help while responders are being found or are on the way.", compact = true)
                    CareAiConversation(ai, incident.description)
                    if (owner || myResponse != null) CareButton("View incident messages", { panel = "messages" }, icon = Icons.Outlined.ChatBubbleOutline)
                    if (owner) {
                        CareButton("Add medical details", { draft = ""; panel = "details" }, icon = Icons.Outlined.Edit)
                        if (presentation.phase != RescuePhase.ARRIVED) TextButton(onClick = { confirmFinish = "resolve" }, enabled = !state.busy && !state.cancellationPending) { Text("Mark incident resolved") }
                    }
                }
            }
        }
        if (!ended) Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CareButton("Call 112", { dial(context, "112") }, Modifier.weight(1f), icon = Icons.Outlined.Phone)
            if (owner || pending) TextButton(onClick = { cancelSos() }, enabled = !state.cancellationPending && (!state.busy || state.submission == SosSubmission.SENDING)) {
                Text("Cancel SOS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (showPrompt) MedicalEmergencyDialog(medical, draft, { draft = it }, prompt::submit, { prompt.cancel(); onBack() }, { dial(context, "112") })
    confirmFinish?.let { action -> AlertDialog(onDismissRequest = { confirmFinish = null },
        title = { Text(if (action == "cancel") "Cancel this medical SOS?" else "Resolve this medical request?") },
        text = { Text(if (action == "cancel") "Use this if the SOS was accidental or help is no longer needed. We’ll confirm cancellation with the server." else "This updates the incident for everyone participating.") },
        confirmButton = { TextButton(onClick = { confirmFinish = null; viewModel.finish(action == "cancel") }) { Text(if (action == "cancel") "Yes, cancel SOS" else "Yes, mark resolved") } },
        dismissButton = { TextButton(onClick = { confirmFinish = null }) { Text("Keep SOS active") } }) }
}

@Composable
internal fun MedicalEmergencyDialog(state: MedicalPromptState, draft: String, onDraft: (String) -> Unit,
    onSubmit: (String) -> Unit, onCancel: () -> Unit, onCall: () -> Unit) {
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(20.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.heightIn(max = 560.dp).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CareHeading("What’s the medical emergency?", "Tap a symptom or condition to send it now.", compact = true)
                CareMuted("${ceil(state.remainingMillis / 1000.0).toInt()}s until a general medical SOS")
                LinearProgressIndicator(progress = { state.remainingMillis / MEDICAL_PROMPT_DURATION_MILLIS.toFloat() }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Chest pain", "Breathing trouble", "Injury / bleeding", "Fainting", "Seizure", "Other / not sure").chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { pair.forEach { label ->
                        OutlinedButton(onClick = { onSubmit(if (label == "Other / not sure") "" else label) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp), shape = RoundedCornerShape(11.dp)) { Text(label) }
                    } }
                }
                CareField("Or describe what happened", draft, { onDraft(it.take(180)) }, hint = "Symptoms or a known condition")
                }
                CareButton("Send medical SOS now", { onSubmit(draft) }, style = CareButtonStyle.SOS, icon = Icons.Outlined.Sos)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onCall) { Text("Call 112") }
                    TextButton(onClick = onCancel) { Text("Cancel SOS") }
                }
            }
        }
    }
}

@Composable
private fun RescueStatus(presentation: IncidentPresentation) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("SOS sent", "Accepted", "On the way", "Arrived").forEachIndexed { index, label ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(4.dp).background(if (index <= presentation.progress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)))
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (index <= presentation.progress) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    CareHeading(when (presentation.phase) {
        RescuePhase.SEARCHING -> "Searching for nearby responders"
        RescuePhase.ACCEPTED -> "Responder accepted"
        RescuePhase.EN_ROUTE -> presentation.primary?.eta?.let { "Arriving in $it" } ?: "Your responder is on the way"
        RescuePhase.ARRIVED -> "Your responder has arrived"
        else -> "Your medical SOS"
    }, if (presentation.phase == RescuePhase.SEARCHING) "Your medical SOS is active. You can ask the AI assistant for help while we wait for someone to accept." else presentation.primary?.name)
}
