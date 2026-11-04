package com.example.nearhelp.ui.assistant

import com.example.nearhelp.i18n.LocalizedText as Text
import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.ContextCompat
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import kotlinx.coroutines.launch

@Composable
fun AiCrisisAssistantScreen(onNavigateBack: () -> Unit, onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToMap: () -> Unit = {}, onNavigateToProfile: () -> Unit = {}, viewModel: AiCrisisAssistantViewModel,
    conditionId: String = "cardiac_arrest", sessionId: String = "", showBottomBar: Boolean = true, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf("home") }
    LaunchedEffect(conditionId, sessionId) { viewModel.initialize(conditionId, sessionId.ifBlank { "consultation-${java.util.UUID.randomUUID()}" }) }
    BackHandler(enabled = page != "home") { page = "home" }
    Column(modifier.fillMaxSize()) {
        CarePage(if (page == "home") null else when (page) { "topics" -> "First-aid topics"; "protocol" -> "Emergency protocol"; "history" -> "Your conversations"; "handover" -> "Paramedic handover"; else -> "AI conversation" },
            if (page != "home") ({ page = "home" }) else null, Modifier.weight(1f)) {
            when (page) {
                "chat" -> CareAiConversation(viewModel)
                "history" -> {
                    CareButton("New conversation", { viewModel.startNewChat(); page = "chat" }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Add)
                    CareHeading("Your conversations")
                    if (state.chatHistory.isEmpty()) CareNote("No conversations yet", "Start a conversation to get help from the assistant.", Icons.Outlined.ChatBubbleOutline)
                    state.chatHistory.forEach { thread ->
                        CareRow(thread.title, thread.timestamp, Icons.Outlined.ChatBubbleOutline) { viewModel.loadChatSession(thread); page = "chat" }
                        CareButton("Delete conversation", { viewModel.deleteChatSession(thread.id) }, style = CareButtonStyle.QUIET, icon = Icons.Outlined.DeleteOutline)
                    }
                }
                "handover" -> {
                    val report = state.handoverSummary
                    if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    if (report == null) CareNote("Report not available", "Try generating the handover again.")
                    else {
                        CareHeading("Paramedic handover")
                        CareMuted("Report ${report.reportId}")
                        CareNote("Clinical summary", report.diagnosticSummary, Icons.Outlined.Description)
                        CareRow("Destination hospital", report.destinationHospital, Icons.Outlined.LocalHospital)
                        CareRow("Incident location", report.emergencyLocation, Icons.Outlined.LocationOn)
                        report.completedProtocolSteps.forEach { CareRow(it, icon = Icons.Outlined.CheckCircle) }
                    }
                    CareButton("Close report", { viewModel.dismissHandoverModal(); page = "protocol" }, style = CareButtonStyle.PRIMARY)
                }
                "protocol" -> {
                    EmergencyCallButton()
                    val protocol = state.protocol
                    if (protocol == null) CareNote("Loading protocol", "Please wait for first-aid guidance.")
                    else {
                        CareHeading(protocol.conditionLabel)
                        CareNote("AI-generated guidance", "Follow the emergency operator’s instructions.", Icons.Outlined.Info)
                        val completed = state.completedStepsMap[protocol.conditionId].orEmpty()
                        val step = protocol.steps.firstOrNull { it.stepNumber !in completed }
                        if (step == null) CareNote("All steps completed", icon = Icons.Outlined.CheckCircle)
                        else {
                            CareMuted("Step ${protocol.steps.indexOf(step) + 1} of ${protocol.steps.size}")
                            Text(step.title, style = MaterialTheme.typography.titleMedium)
                            Text(step.actionInstruction, style = MaterialTheme.typography.bodyLarge)
                            step.warningNote?.takeIf { it.isNotBlank() }?.let { CareNote("Precaution", it, Icons.Outlined.WarningAmber, error = true) }
                            CareButton("Mark step complete", { viewModel.toggleStep(protocol.conditionId, step.stepNumber) }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Check)
                        }
                        CareButton("Ask about this step", { page = "chat" }, icon = Icons.Outlined.ChatBubbleOutline)
                        protocol.citations.forEach { CareMuted("${it.authority} · ${it.section}") }
                        CareButton("View handover report", { viewModel.requestHandover(); page = "handover" }, style = CareButtonStyle.QUIET, icon = Icons.AutoMirrored.Outlined.Assignment)
                    }
                }
                "topics" -> {
                    CareHeading("First-aid topics")
                    state.protocols.forEach { protocol -> CareRow(protocol.conditionLabel, icon = Icons.AutoMirrored.Outlined.MenuBook) { viewModel.selectProtocol(protocol.conditionId); page = "protocol" } }
                }
                else -> {
                    CareHeading("How can I help?", "Ask about first aid or choose a topic.")
                    CareNote("AI guidance may be inaccurate", "For urgent help, call emergency services.")
                    CareButton("Start a conversation", { page = "chat" }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.ChatBubbleOutline)
                    Text("First-aid topics", style = MaterialTheme.typography.titleMedium)
                    if (state.isLoading && state.protocols.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
                    state.protocols.take(4).forEach { protocol -> CareRow(protocol.conditionLabel, icon = Icons.AutoMirrored.Outlined.MenuBook) { viewModel.selectProtocol(protocol.conditionId); page = "protocol" } }
                    CareButton("View all ${state.protocols.size} topics", { page = "topics" }, style = CareButtonStyle.QUIET)
                    CareRow("Recent conversations", "Continue a previous conversation", Icons.Outlined.History) { page = "history" }
                }
            }
        }
        if (showBottomBar) VictimBottomNavBar(VictimNavTab.CHAT, { when (it) { VictimNavTab.HOME -> onNavigateToHome(); VictimNavTab.CHAT -> {}; VictimNavTab.MAP -> onNavigateToMap(); VictimNavTab.PROFILE -> onNavigateToProfile() } })
    }
}

/** The same conversation is used in Assistant and inside an active incident; leaving the panel never clears it. */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CareAiConversation(viewModel: AiCrisisAssistantViewModel, emergencyDescription: String? = null) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var message by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val speechLanguage = LocalConfiguration.current.locales[0].toLanguageTag()
    var attachment by remember { mutableStateOf<AssistantAttachment?>(null) }
    var readingAttachment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var attachmentError by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                readingAttachment = true; attachmentError = null
                try { attachment = readAssistantAttachment(context, uri) }
                catch (e: kotlinx.coroutines.CancellationException) { throw e }
                catch (e: Exception) { attachmentError = e.message ?: "Could not read this attachment. Please type your question instead." }
                finally { readingAttachment = false }
            }
        }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { attachment = cameraAttachment(it); attachmentError = null; it.recycle() }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) runCatching { camera.launch(null) }.onFailure { attachmentError = "Camera is unavailable on this device." }
        else attachmentError = "Allow camera access to attach a photo, or describe the situation in words."
    }
    val preview = remember(attachment?.jpeg) { attachment?.jpeg?.let { encoded -> runCatching { val bytes = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP); android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull() } }
    DisposableEffect(preview) { onDispose { preview?.recycle() } }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { message = it }
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.chatMessages.isEmpty()) ChatBubble(false, "NearHelp AI Assistant", com.example.nearhelp.i18n.uiText("I’m here to help. Tell me what’s happening."))
        state.chatMessages.forEach { msg ->
            ChatBubble(msg.isUser, if (msg.isUser) "You" else "NearHelp AI Assistant", msg.text)
            msg.contraindications.forEach { CareNote(it.warningTitle, "${it.warningMessage}\n${it.actionDirective}", Icons.Outlined.WarningAmber, true) }
            msg.citations.forEach { CareMuted("${it.authority} · ${it.section}") }
        }
        if (state.isLoading) { LinearProgressIndicator(Modifier.fillMaxWidth()); CareMuted("The assistant is preparing a response…") }
        state.error?.let { CareNote("Could not process your message", it, error = true) }
        if (readingAttachment) LinearProgressIndicator(Modifier.fillMaxWidth())
        attachmentError?.let { CareNote("Attachment unavailable", it, error = true) }
        attachment?.let { CareNote("Attachment ready", it.name, Icons.Outlined.Description); if (preview != null) Image(preview.asImageBitmap(), "Attached photo or PDF first-page preview", Modifier.fillMaxWidth().heightIn(max = 200.dp)); CareButton("Remove attachment", { attachment = null }, style = CareButtonStyle.QUIET) }
        CareField("Ask the AI assistant", message, { message = it.take(4000) }, multiline = true, hint = "What do you need help with?")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { picker.launch(arrayOf("text/plain", "application/pdf", "image/*")) }, enabled = !readingAttachment && !state.isLoading) { Icon(Icons.Outlined.AttachFile, null); Text("Attach") }
            TextButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) runCatching { camera.launch(null) }.onFailure { attachmentError = "Camera is unavailable on this device." }
                else cameraPermission.launch(Manifest.permission.CAMERA)
            }, enabled = !readingAttachment && !state.isLoading) { Icon(Icons.Outlined.CameraAlt, null); Text("Camera") }
            TextButton(onClick = { runCatching { speech.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguage)) }.onFailure { attachmentError = "Voice input is unavailable on this device." } }) { Icon(Icons.Outlined.MicNone, null); Text("Voice") }
        }
        CareButton("Send to AI assistant", {
            val content = attachment
            val question = message.ifBlank { "Please help me understand the attached photo or document." }
            val text = if (content?.text.isNullOrBlank()) question else "$question\n\nDocument contents:\n${content.text}"
            viewModel.sendChatMessage(text, content?.jpeg, emergencyDescription); message = ""; attachment = null
        }, style = CareButtonStyle.PRIMARY, enabled = !state.isLoading && !readingAttachment && (message.isNotBlank() || attachment != null), icon = Icons.AutoMirrored.Outlined.Send)
        CareMuted("AI guidance may be inaccurate. For urgent help, call emergency services.")
    }
}

@Composable
private fun ChatBubble(user: Boolean, sender: String, text: String) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        Column(Modifier.fillMaxWidth(.94f).background(if (user) c.primary else c.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(sender, style = MaterialTheme.typography.labelMedium, color = if (user) c.onPrimary else c.onSurfaceVariant)
            androidx.compose.material3.Text(text, style = MaterialTheme.typography.bodyLarge, color = if (user) c.onPrimary else c.onSurface)
        }
    }
}
