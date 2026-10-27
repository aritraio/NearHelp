package com.example.nearhelp.ui.incident

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.data.location.LocationHelper
import com.example.nearhelp.data.api.ws.ConnectionStatus
import com.example.nearhelp.notifications.PushRegistration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IncidentScreen(
    initialId: String = "",
    createMode: Boolean = false,
    onBack: () -> Unit,
    viewModel: IncidentViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context.applicationContext) }
    val location by locationHelper.locationState.collectAsStateWithLifecycle()
    var showCreateForm by rememberSaveable { mutableStateOf(createMode) }
    var available by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("medical") }
    var notes by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var confirmFinish by remember { mutableStateOf<Boolean?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        locationHelper.startLocationUpdates()
        PushRegistration.sync()
    }
    LaunchedEffect(Unit) {
        if (!showCreateForm || viewModel.activeId != null) viewModel.start(initialId)
        val required = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) required += Manifest.permission.POST_NOTIFICATIONS
        permission.launch(required.toTypedArray())
        PushRegistration.sync()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { locationHelper.startLocationUpdates() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { locationHelper.stopLocationUpdates() }
    DisposableEffect(Unit) { onDispose { locationHelper.close() } }
    LaunchedEffect(location.latitude, location.longitude, location.hasFix, available, state.role, state.connection) {
        if (location.hasFix) viewModel.updateLocation(location, available)
    }
    LaunchedEffect(available) {
        while (available) {
            delay(60_000)
            if (location.hasFix) viewModel.updateLocation(location, true)
        }
    }
    BackHandler(enabled = state.busy) {}
    val incident = state.incident
    val ended = incident?.status in listOf("RESOLVED", "CANCELLED")
    val owner = incident != null && incident.broadcasterId == viewModel.userId
    val responder = incident?.responses?.any { it.responderId == viewModel.userId && it.status != "DECLINED" } == true
    val creating = showCreateForm && viewModel.activeId == null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(if (creating) "Request help" else if (incident != null) "Live incident" else "Community emergencies") },
                navigationIcon = { IconButton(onClick = onBack, enabled = !state.busy) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } })
        },
        bottomBar = {
            if (incident != null && !ended && (owner || responder)) {
                Surface(tonalElevation = 3.dp) {
                    Row(Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(12.dp)) {
                        OutlinedTextField(value = message, onValueChange = { if (it.length <= 1000) message = it },
                            label = { Text("Message participants") }, modifier = Modifier.weight(1f), maxLines = 3)
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { if (viewModel.send(message)) message = "" },
                            enabled = message.isNotBlank() && state.connection == ConnectionStatus.CONNECTED) { Text("Send") }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) }, modifier = Modifier.fillMaxWidth()) { Text("Call emergency services · 112") }
            }
            if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) { Text(error); TextButton(onClick = { if (viewModel.activeId != null) viewModel.reconnect() else if (!creating) run { showCreateForm = false; viewModel.browse() } }) { Text("Retry connection") } }
            } } }
            if (creating) {
                item { Text("Your location is shared with people who respond to this incident. If sending times out, retry to recover the same SOS with its original details.", style = MaterialTheme.typography.bodyLarge) }
                item { FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("medical", "fire", "crime", "accident").forEach { value ->
                        FilterChip(selected = category == value, onClick = { category = value }, label = { Text(value.replaceFirstChar { it.uppercase() }) })
                    }
                } }
                item { OutlinedTextField(value = notes, onValueChange = { if (it.length <= 2048) notes = it }, label = { Text("What happened? (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
                item { OutlinedTextField(value = address, onValueChange = { if (it.length <= 512) address = it }, label = { Text("Address or landmark (optional)") }, supportingText = { Text("GPS: ${if (location.hasFix) location.coordinatesText else "waiting for location"}") }, modifier = Modifier.fillMaxWidth()) }
                item { Button(onClick = { viewModel.create(location, category, notes, address.ifBlank { location.localityName }) }, enabled = !state.busy && location.hasFix, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Send SOS") } }
                if (!location.hasFix) item { Text("Enable location services and allow location access to send an accurate SOS."); TextButton(onClick = { permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) { Text("Enable location") } }
            } else if (incident == null && viewModel.activeId == null) {
                item { Card { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("Available to help", style = MaterialTheme.typography.titleMedium); Text("Share your location for nearby alerts.") }
                    Switch(checked = available, onCheckedChange = { available = it })
                } } }
                item { Text(if (PushRegistration.configured()) "Push configured. Allow notifications in Android settings to receive alerts." else "Push setup is missing. Keep this feed open to discover incidents; it refreshes every 10 seconds.") }
                item { Text("Open an incident to see its details and choose whether to respond.", style = MaterialTheme.typography.bodyLarge) }
                if (state.feed.isEmpty()) item { Text("No active incidents loaded. The feed refreshes every 10 seconds.") }
                items(state.feed, key = { it.id }) { event -> Card(onClick = { viewModel.open(event.id) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${event.crisisType.replaceFirstChar { it.uppercase() }} · Level ${event.severityLevel}", style = MaterialTheme.typography.titleMedium)
                        Text(event.address ?: "Location available in incident")
                        Text(event.status.replace('_', ' '))
                        Text("Open incident →")
                    }
                } }
            } else if (incident == null) {
                item { Text("Loading incident…"); TextButton(onClick = { run { showCreateForm = false; viewModel.browse() } }) { Text("Browse active incidents") } }
            } else {
                item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${incident.crisisType.replaceFirstChar { it.uppercase() }} · ${incident.status.replace('_', ' ')}", style = MaterialTheme.typography.titleLarge)
                    Text(incident.address ?: "${incident.latitude}, ${incident.longitude}")
                    incident.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
                    Text("Incident ${incident.id}", style = MaterialTheme.typography.bodySmall)
                    Text("Connection: ${state.connection.name.lowercase()}")
                    if (state.connection != ConnectionStatus.CONNECTED) TextButton(onClick = viewModel::reconnect) { Text("Reconnect chat and tracking") }
                    OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${incident.latitude},${incident.longitude}"))) }) { Text("Open incident location in Maps") }
                } } }
                if (!ended && !owner && !responder) item { Button(onClick = { viewModel.respond(location) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("I'm responding") } }
                if (responder && !ended) item { Text("Your GPS is shared while this screen is open. ${if (location.hasFix) location.coordinatesText else "Waiting for device location."}") }
                item { Text("Responders", style = MaterialTheme.typography.titleMedium) }
                if (incident.responses.none { it.status != "DECLINED" }) item { Text("No responder has accepted yet. Use emergency services if help is urgent.") }
                items(incident.responses.filter { it.status != "DECLINED" }, key = { it.responderId }) { response ->
                    val position = state.snapshot?.responders?.firstOrNull { it.responderId == response.responderId }
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                        Text(response.responderName, style = MaterialTheme.typography.titleMedium)
                        Text(position?.let { "${it.status} · ${it.distanceFormatted} · ETA ${it.etaFormatted}" } ?: "${response.status} · waiting for GPS")
                        position?.let { Text("${it.latitude}, ${it.longitude}", style = MaterialTheme.typography.bodySmall) }
                    } }
                }
                if (owner && !ended) item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { confirmFinish = true }, enabled = !state.busy) { Text("Cancel SOS") }
                    Button(onClick = { confirmFinish = false }, enabled = !state.busy) { Text("Mark resolved") }
                } }
                item { Text("Incident chat & timeline", style = MaterialTheme.typography.titleMedium) }
                items(incident.timeline, key = { it.id }) { event -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                    Text(event.actorName ?: "NearHelp", style = MaterialTheme.typography.labelLarge)
                    Text(if (event.eventType == "chat_message") event.details["text"]?.toString().orEmpty() else event.eventType.replace('_', ' '))
                    Text(event.timestamp, style = MaterialTheme.typography.bodySmall)
                } } }
                item { TextButton(onClick = { run { showCreateForm = false; viewModel.browse() } }) { Text("Browse other incidents") } }
            }
        }
    }
    confirmFinish?.let { cancel -> AlertDialog(onDismissRequest = { confirmFinish = null }, title = { Text(if (cancel) "Cancel this SOS?" else "Resolve this incident?") }, text = { Text("This updates the incident for everyone participating.") }, confirmButton = { TextButton(onClick = { confirmFinish = null; viewModel.finish(cancel) }) { Text("Confirm") } }, dismissButton = { TextButton(onClick = { confirmFinish = null }) { Text("Keep active") } }) }
}
