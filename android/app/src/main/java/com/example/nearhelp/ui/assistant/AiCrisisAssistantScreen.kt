package com.example.nearhelp.ui.assistant

import com.example.nearhelp.i18n.LocalizedText as Text
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab

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
fun CareAiConversation(viewModel: AiCrisisAssistantViewModel, emergencyDescription: String? = null) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var message by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.chatMessages.isEmpty()) ChatBubble(false, "NearHelp AI Assistant", com.example.nearhelp.i18n.uiText("I’m here to help. Tell me what’s happening."))
        state.chatMessages.forEach { msg ->
            ChatBubble(msg.isUser, if (msg.isUser) "You" else "NearHelp AI Assistant", msg.text)
            msg.contraindications.forEach { CareNote(it.warningTitle, "${it.warningMessage}\n${it.actionDirective}", Icons.Outlined.WarningAmber, true) }
            msg.citations.forEach { CareMuted("${it.authority} · ${it.section}") }
        }
        if (state.isLoading) { LinearProgressIndicator(Modifier.fillMaxWidth()); CareMuted("The assistant is preparing a response…") }
        state.error?.let { CareNote("Could not process your message", it, error = true) }
        CareField("Ask the AI assistant", message, { message = it.take(4000) }, multiline = true, hint = "What do you need help with?")
        CareButton(
            "Send to AI assistant",
            {
                val text = message.trim()
                if (text.isNotBlank()) {
                    viewModel.sendChatMessage(text, emergencyDescription = emergencyDescription)
                    message = ""
                }
            },
            style = CareButtonStyle.PRIMARY,
            enabled = !state.isLoading && message.isNotBlank(),
            icon = Icons.AutoMirrored.Outlined.Send
        )
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
