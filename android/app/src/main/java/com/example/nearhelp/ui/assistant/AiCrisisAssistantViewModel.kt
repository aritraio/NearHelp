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

fun defaultChatHistory(): List<ChatHistorySession> = emptyList()

data class AiCrisisAssistantUiState(
  val sessionId: String = "DEMO-SESSION-001",
  val activeThreadId: String? = null,
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
  val error: String? = null,
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

  private var initializationRequest: Pair<String, String>? = null
  private var initializationJob: Job? = null

  fun initialize(conditionId: String = "cardiac_arrest", sessionId: String = "DEMO-SESSION-001") {
    val request = conditionId to sessionId
    // Returning to a composed tab must not reload or replace the user's selected protocol.
    if (initializationRequest == request) return
    initializationRequest = request
    initializationJob?.cancel()
    _uiState.update { it.copy(conditionId = conditionId, sessionId = sessionId, isLoading = true) }
    initializationJob = viewModelScope.launch {
      val all = repository.getAllProtocols()
      val activeProto = all.firstOrNull { it.conditionId == conditionId } ?: all.firstOrNull() ?: repository.getProtocol(conditionId)
      _uiState.update { state ->
        state.copy(
          protocols = all,
          protocol = activeProto,
          isLoading = false
        )
      }
    }.also { job ->
      job.invokeOnCompletion { cause ->
        if (cause != null && initializationRequest == request) {
          initializationRequest = null
          _uiState.update { it.copy(isLoading = false) }
        }
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
      val questions = (repository as? com.example.nearhelp.data.repository.AiAgentRepository)?.getQuickQuestionsForCondition(conditionId)
      _uiState.update { state ->
        state.copy(
          conditionId = conditionId,
          protocol = found,
          quickQuestions = questions ?: state.quickQuestions
        )
      }
    }
  }

  fun setChatDrawerOpen(isOpen: Boolean) {
    if (!isOpen) {
      closeChatDrawer()
    } else {
      _uiState.update { it.copy(isChatDrawerOpen = true) }
    }
  }

  fun closeChatDrawer() {
    _uiState.update { state ->
      val updatedHistory = if (state.chatMessages.isNotEmpty() && state.activeThreadId != null) {
        updateHistoryWithThread(
          history = state.chatHistory,
          threadId = state.activeThreadId,
          messages = state.chatMessages,
          timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
      } else {
        state.chatHistory
      }
      state.copy(
        isChatDrawerOpen = false,
        activeThreadId = null,
        chatMessages = emptyList(),
        chatHistory = updatedHistory
      )
    }
  }

  fun setHistorySidePanelOpen(isOpen: Boolean) {
    _uiState.update { it.copy(isHistorySidePanelOpen = isOpen) }
  }

  fun loadChatSession(session: ChatHistorySession) {
    _uiState.update {
      it.copy(
        activeThreadId = session.id,
        chatMessages = session.messages,
        isHistorySidePanelOpen = false,
        isChatDrawerOpen = true
      )
    }
  }

  fun startNewChat() {
    _uiState.update {
      it.copy(
        activeThreadId = null,
        chatMessages = emptyList(),
        isHistorySidePanelOpen = false,
        isChatDrawerOpen = true
      )
    }
  }

  fun deleteChatSession(threadId: String) {
    _uiState.update { state ->
      val updatedHistory = state.chatHistory.filterNot { it.id == threadId }
      val isCurrent = state.activeThreadId == threadId
      state.copy(
        chatHistory = updatedHistory,
        chatMessages = if (isCurrent) emptyList() else state.chatMessages,
        activeThreadId = if (isCurrent) null else state.activeThreadId
      )
    }
  }

  fun dismissContraindication() {
    _uiState.update { it.copy(activeContraindication = null) }
  }

  private fun updateHistoryWithThread(
    history: List<ChatHistorySession>,
    threadId: String,
    messages: List<AiChatMessageUiModel>,
    timestamp: String
  ): List<ChatHistorySession> {
    if (messages.isEmpty()) return history
    val firstUserMsg = messages.firstOrNull { it.isUser }?.text ?: messages.first().text
    val title = firstUserMsg.take(36).let { if (it.length >= 36) "$it..." else it }
    val snippet = messages.lastOrNull()?.text?.take(60) ?: ""
    val existing = history.firstOrNull { it.id == threadId }
    val group = existing?.group ?: "Today"
    val updatedSession = ChatHistorySession(
      id = threadId,
      title = existing?.title ?: title,
      snippet = snippet,
      timestamp = "Today, $timestamp",
      group = group,
      messages = messages
    )
    val remaining = history.filterNot { it.id == threadId }
    return listOf(updatedSession) + remaining
  }

  fun sendChatMessage(text: String, imageBase64: String? = null) {
    if (text.isBlank()) return

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val nowStr = timeFormat.format(Date())

    val threadId = _uiState.value.activeThreadId ?: _uiState.value.sessionId.takeIf { runCatching { java.util.UUID.fromString(it) }.isSuccess } ?: "thread-${System.currentTimeMillis()}"

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
        activeThreadId = threadId,
        isChatDrawerOpen = true,
        chatMessages = updatedMessages,
        isLoading = true,
        error = null,
        chatHistory = updateHistoryWithThread(it.chatHistory, threadId, updatedMessages, nowStr)
      )
    }

    viewModelScope.launch {
      try {
        val inputText = if (imageBase64 == null) text else {
          val imageDescription = repository.describeImage(imageBase64).getOrThrow()
          "$text\n\nImage description from automated analysis (may be inaccurate): $imageDescription"
        }
        val response = repository.chatWithAgent(
          sessionId = threadId,
          text = inputText,
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

        val finalMessages = updatedMessages + aiMsg
        _uiState.update { state ->
          if (state.activeThreadId == threadId) {
            state.copy(
              chatMessages = finalMessages,
              activeContraindication = response.contraindications.firstOrNull(),
              quickQuestions = if (response.suggestedQuickQuestions.isNotEmpty()) response.suggestedQuickQuestions else state.quickQuestions,
              isLoading = false,
              chatHistory = updateHistoryWithThread(state.chatHistory, threadId, finalMessages, nowStr)
            )
          } else {
            state.copy(
              isLoading = false,
              chatHistory = updateHistoryWithThread(state.chatHistory, threadId, finalMessages, nowStr)
            )
          }
        }
      } catch (e: kotlinx.coroutines.CancellationException) { throw e }
      catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message ?: "Your message could not be processed. Please try again.") } }
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
