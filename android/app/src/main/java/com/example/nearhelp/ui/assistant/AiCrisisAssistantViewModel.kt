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
  val isHandoverModalOpen: Boolean = false,
  val handoverSummary: ClinicalHandoverSummaryDto? = null,
  val isLoading: Boolean = false,
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

  fun dismissContraindication() {
    _uiState.update { it.copy(activeContraindication = null) }
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

    _uiState.update { it.copy(chatMessages = it.chatMessages + userMsg, isLoading = true) }

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

      _uiState.update { state ->
        state.copy(
          chatMessages = state.chatMessages + aiMsg,
          activeContraindication = response.contraindications.firstOrNull(),
          isLoading = false
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
