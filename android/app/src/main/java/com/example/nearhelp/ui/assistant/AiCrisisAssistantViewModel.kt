package com.example.nearhelp.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nearhelp.data.model.AiChatMessageUiModel
import com.example.nearhelp.data.model.CitationDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.ContraindicationAlertDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import com.example.nearhelp.data.repository.IAiAgentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatHistorySession(
  val id: String,
  val title: String,
  val snippet: String,
  val timestamp: String,
  val group: String,
  val messages: List<AiChatMessageUiModel> = emptyList()
)

fun defaultChatHistory(): List<ChatHistorySession> = listOf(
  ChatHistorySession(
    id = "hist-cpr",
    title = "CPR Compressions & Cadence",
    snippet = "Compress 5 to 6 cm deep at 110-120 BPM cadence in center of chest...",
    timestamp = "Today, 10:15",
    group = "Today",
    messages = listOf(
      AiChatMessageUiModel("u-1", true, "Bystander", "How deep should chest compressions be?", "10:14"),
      AiChatMessageUiModel(
        "a-1", false, "NearHelp Emergency Agent",
        "✅ Compress 5 to 6 cm (approx 2 inches) deep at a cadence of 110–120 compressions/minute in the center of the breastbone. Allow full recoil between pushes.\n\n[Source: AHA CPR Guidelines 2020 §3.2 • IRC BLS 2020]",
        "10:15",
        highlightBadge = "AHA / IRC Guideline (110 BPM)"
      )
    )
  ),
  ChatHistorySession(
    id = "hist-bleeding",
    title = "Severe Bleeding & Direct Pressure",
    snippet = "Apply continuous direct pressure with sterile gauze...",
    timestamp = "Yesterday, 17:30",
    group = "Yesterday",
    messages = listOf(
      AiChatMessageUiModel("u-2", true, "Bystander", "How to manage heavy bleeding from an arm cut?", "17:29"),
      AiChatMessageUiModel(
        "a-2", false, "NearHelp Emergency Agent",
        "⚠️ Apply firm, continuous direct pressure with sterile gauze. Elevate above heart level. If blood soaks through, do NOT remove original gauze—apply more layers on top.\n\n[Source: International Red Cross Trauma Protocols]",
        "17:30",
        highlightBadge = "Trauma Management"
      )
    )
  ),
  ChatHistorySession(
    id = "hist-burn",
    title = "Thermal Burn First Aid",
    snippet = "Cool the burn under cool running tap water for 10-20 min...",
    timestamp = "Sep 10",
    group = "Earlier",
    messages = listOf(
      AiChatMessageUiModel("u-3", true, "Bystander", "Can I put ice or butter on a boiling oil burn?", "14:02"),
      AiChatMessageUiModel(
        "a-3", false, "NearHelp Emergency Agent",
        "❌ NEVER use bare ice, butter, or toothpaste. Cool the burn immediately under cool, gentle running tap water for 10–20 minutes. Cover loosely with sterile dressing.\n\n[Source: WHO Burns Management Guide]",
        "14:03",
        highlightBadge = "Contraindicated Action"
      )
    )
  ),
  ChatHistorySession(
    id = "hist-choking",
    title = "Choking & Heimlich Protocol",
    snippet = "5 back blows between shoulder blades followed by 5 abdominal thrusts...",
    timestamp = "Sep 5",
    group = "Earlier",
    messages = listOf(
      AiChatMessageUiModel("u-4", true, "Bystander", "Person is choking and holding their throat", "11:20"),
      AiChatMessageUiModel(
        "a-4", false, "NearHelp Emergency Agent",
        "🚨 Perform 5 back blows between shoulder blades followed by 5 abdominal thrusts (Heimlich maneuver). If victim becomes unresponsive, lower safely and begin CPR.\n\n[Source: AHA Airway Obstruction Guidelines]",
        "11:21",
        highlightBadge = "Airway Emergency"
      )
    )
  )
)

data class AiCrisisAssistantUiState(
  val sessionId: String = "DEMO-SESSION-001",
  val conditionId: String = "cardiac_arrest",
  val protocol: GroundedProtocolDto? = null,
  val protocols: List<GroundedProtocolDto> = emptyList(),
  val completedSteps: List<Int> = emptyList(),
  val completedStepsMap: Map<String, Set<Int>> = emptyMap(),
  val currentStepIndex: Int = 0,
  val chatMessages: List<AiChatMessageUiModel> = emptyList(),
  val activeContraindication: ContraindicationAlertDto? = null,
  val isChatDrawerOpen: Boolean = false,
  val isHistorySidePanelOpen: Boolean = false,
  val isHandoverModalOpen: Boolean = false,
  val handoverSummary: ClinicalHandoverSummaryDto? = null,
  val isLoading: Boolean = false,
  val chatHistory: List<ChatHistorySession> = defaultChatHistory(),
  val quickQuestions: List<String> = listOf(
    "Can I give water or oral medicine?",
    "How deep should chest compressions be?",
    "When and how do I use the AED?",
    "What if ribs crack during CPR?",
    "Am I legally protected if I help?"
  )
)

class AiCrisisAssistantViewModel(
  private val repository: IAiAgentRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AiCrisisAssistantUiState())
  val uiState: StateFlow<AiCrisisAssistantUiState> = _uiState.asStateFlow()

  fun initialize(conditionId: String = "cardiac_arrest", sessionId: String = "DEMO-SESSION-001") {
    _uiState.update { it.copy(conditionId = conditionId, sessionId = sessionId, isLoading = true) }
    viewModelScope.launch {
      val all = repository.getAllProtocols()
      val activeProto = all.firstOrNull { it.conditionId == conditionId } ?: all.firstOrNull() ?: repository.getProtocol(conditionId)
      _uiState.update { state ->
        state.copy(
          protocols = all,
          protocol = activeProto,
          isLoading = false
        )
      }
    }
  }

  fun toggleStep(stepNumber: Int) {
    val activeCond = _uiState.value.conditionId
    toggleStep(activeCond, stepNumber)
  }

  fun toggleStep(conditionId: String, stepNumber: Int) {
    _uiState.update { state ->
      val currentSet = state.completedStepsMap[conditionId] ?: emptySet()
      val updatedSet = if (currentSet.contains(stepNumber)) {
        currentSet - stepNumber
      } else {
        currentSet + stepNumber
      }
      val updatedMap = state.completedStepsMap + (conditionId to updatedSet)
      val legacyCompleted = if (conditionId == state.conditionId) updatedSet.toList() else state.completedSteps
      val nextIdx = minOf(updatedSet.size, (state.protocol?.steps?.size ?: 1) - 1)
      state.copy(
        completedStepsMap = updatedMap,
        completedSteps = legacyCompleted,
        currentStepIndex = nextIdx
      )
    }
  }

  fun selectProtocol(conditionId: String) {
    val found = _uiState.value.protocols.firstOrNull { it.conditionId == conditionId }
    if (found != null) {
      _uiState.update { it.copy(conditionId = conditionId, protocol = found) }
    }
  }

  fun setChatDrawerOpen(isOpen: Boolean) {
    _uiState.update { it.copy(isChatDrawerOpen = isOpen) }
  }

  fun setHistorySidePanelOpen(isOpen: Boolean) {
    _uiState.update { it.copy(isHistorySidePanelOpen = isOpen) }
  }

  fun loadChatSession(session: ChatHistorySession) {
    _uiState.update {
      it.copy(
        chatMessages = session.messages,
        isHistorySidePanelOpen = false,
        isChatDrawerOpen = true
      )
    }
  }

  fun startNewChat() {
    _uiState.update {
      it.copy(
        chatMessages = emptyList(),
        isHistorySidePanelOpen = false,
        isChatDrawerOpen = true
      )
    }
  }

  fun dismissContraindication() {
    _uiState.update { it.copy(activeContraindication = null) }
  }

  private fun updateHistoryWithCurrentChat(
    history: List<ChatHistorySession>,
    messages: List<AiChatMessageUiModel>,
    timestamp: String
  ): List<ChatHistorySession> {
    if (messages.isEmpty()) return history
    val firstUserMsg = messages.firstOrNull { it.isUser }?.text ?: messages.first().text
    val title = firstUserMsg.take(36).let { if (it.length >= 36) "$it..." else it }
    val snippet = messages.lastOrNull()?.text?.take(60) ?: ""
    val currentSession = ChatHistorySession(
      id = "active-session",
      title = title,
      snippet = snippet,
      timestamp = "Today, $timestamp",
      group = "Today",
      messages = messages
    )
    val remaining = history.filterNot { it.id == "active-session" }
    return listOf(currentSession) + remaining
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val nowStr = timeFormat.format(Date())

    val userMsg = AiChatMessageUiModel(
      id = "user-${System.currentTimeMillis()}",
      isUser = true,
      senderName = "Bystander",
      text = text,
      timestamp = nowStr
    )

    val updatedMessages = _uiState.value.chatMessages + userMsg
    _uiState.update {
      it.copy(
        chatMessages = updatedMessages,
        isLoading = true,
        chatHistory = updateHistoryWithCurrentChat(it.chatHistory, updatedMessages, nowStr)
      )
    }

    viewModelScope.launch {
      val response = repository.chatWithAgent(
        sessionId = _uiState.value.sessionId,
        text = text,
        currentStepIndex = _uiState.value.currentStepIndex,
        completedSteps = _uiState.value.completedSteps
      )

      val aiMsg = AiChatMessageUiModel(
        id = "ai-${System.currentTimeMillis()}",
        isUser = false,
        senderName = "NearHelp Emergency Agent",
        text = response.replyText,
        timestamp = nowStr,
        highlightBadge = response.highlightText,
        citations = response.citations,
        contraindications = response.contraindications
      )

      val finalMessages = _uiState.value.chatMessages + aiMsg
      _uiState.update { state ->
        state.copy(
          chatMessages = finalMessages,
          activeContraindication = response.contraindications.firstOrNull(),
          isLoading = false,
          chatHistory = updateHistoryWithCurrentChat(state.chatHistory, finalMessages, nowStr)
        )
      }
    }
  }

  fun requestHandover() {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val handover = repository.generateHandover(_uiState.value.sessionId)
      _uiState.update { it.copy(handoverSummary = handover, isHandoverModalOpen = true, isLoading = false) }
    }
  }

  fun dismissHandoverModal() {
    _uiState.update { it.copy(isHandoverModalOpen = false) }
  }

  override fun onCleared() {
    super.onCleared()
  }
}
