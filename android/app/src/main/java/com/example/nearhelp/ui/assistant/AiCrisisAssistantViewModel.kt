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
  val completedSteps: List<Int> = emptyList(),
  val currentStepIndex: Int = 0,
  val cprMetronomeActive: Boolean = false,
  val cprBeatCount: Long = 0,
  val isHeartPulse: Boolean = false,
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

  private var metronomeJob: Job? = null

  fun initialize(conditionId: String = "cardiac_arrest", sessionId: String = "DEMO-SESSION-001") {
    _uiState.update { it.copy(conditionId = conditionId, sessionId = sessionId, isLoading = true) }
    viewModelScope.launch {
      val proto = repository.getProtocol(conditionId)
      _uiState.update { state ->
        state.copy(
          protocol = proto,
          isLoading = false,
          cprMetronomeActive = proto.cprBpm != null && proto.cprBpm > 0
        )
      }
      if (proto.cprBpm != null && proto.cprBpm > 0) {
        startMetronomeTimer(proto.cprBpm)
      }
    }
  }

  fun toggleStep(stepNumber: Int) {
    _uiState.update { state ->
      val updated = if (state.completedSteps.contains(stepNumber)) {
        state.completedSteps - stepNumber
      } else {
        state.completedSteps + stepNumber
      }
      val nextIdx = minOf(updated.size, (state.protocol?.steps?.size ?: 1) - 1)
      state.copy(completedSteps = updated, currentStepIndex = nextIdx)
    }
  }

  fun toggleCprMetronome() {
    val isActive = !_uiState.value.cprMetronomeActive
    _uiState.update { it.copy(cprMetronomeActive = isActive) }
    if (isActive) {
      val bpm = _uiState.value.protocol?.cprBpm ?: 110
      startMetronomeTimer(bpm)
    } else {
      stopMetronomeTimer()
    }
  }

  private fun startMetronomeTimer(bpm: Int) {
    stopMetronomeTimer()
    val periodMs = (60000.0 / bpm).toLong() // 545ms for 110 BPM
    metronomeJob = viewModelScope.launch {
      while (isActive && _uiState.value.cprMetronomeActive) {
        _uiState.update { it.copy(isHeartPulse = true, cprBeatCount = it.cprBeatCount + 1) }
        delay(120)
        _uiState.update { it.copy(isHeartPulse = false) }
        delay(periodMs - 120)
      }
    }
  }

  private fun stopMetronomeTimer() {
    metronomeJob?.cancel()
    metronomeJob = null
    _uiState.update { it.copy(isHeartPulse = false) }
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
    stopMetronomeTimer()
  }
}
