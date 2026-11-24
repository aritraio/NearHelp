package com.example.nearhelp.ui.assistant

import com.example.nearhelp.data.model.AgentChatResponseDto
import com.example.nearhelp.data.model.ClinicalHandoverSummaryDto
import com.example.nearhelp.data.model.GroundedProtocolDto
import com.example.nearhelp.data.repository.IAiAgentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiCrisisAssistantViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = ProtocolRepository()
    private lateinit var viewModel: AiCrisisAssistantViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = AiCrisisAssistantViewModel(repository)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun returningToTab_preservesSelectionAndDoesNotReload() = runTest {
        viewModel.initialize("cardiac_arrest", "session")
        advanceUntilIdle()
        viewModel.selectProtocol("burns")
        viewModel.toggleStep("burns", 1)

        viewModel.initialize("cardiac_arrest", "session")
        advanceUntilIdle()

        assertEquals(1, repository.loads)
        assertEquals("burns", viewModel.uiState.value.conditionId)
        assertEquals(setOf(1), viewModel.uiState.value.completedStepsMap["burns"])
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun repeatedInitializationWhileLoading_doesNotStartDuplicateWork() = runTest {
        viewModel.initialize("cardiac_arrest", "session")
        runCurrent()
        viewModel.initialize("cardiac_arrest", "session")
        advanceUntilIdle()
        assertEquals(1, repository.loads)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun newSessionDuringLoading_cancelsOldInitialization() = runTest {
        viewModel.initialize("cardiac_arrest", "old")
        runCurrent()
        viewModel.initialize("burns", "new")
        advanceUntilIdle()
        assertEquals(2, repository.loads)
        assertEquals("new", viewModel.uiState.value.sessionId)
        assertEquals("burns", viewModel.uiState.value.protocol?.conditionId)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun photoAttachment_isAnalyzedBeforeSendingItsContextToChat() = runTest {
        viewModel.sendChatMessage("Help me understand this document", "photo-bytes")
        advanceUntilIdle()
        assertEquals("photo-bytes", repository.image)
        assertTrue(repository.chatText.contains("Image description"))
        assertTrue(repository.chatText.contains("A first-aid document"))
        assertEquals(2, viewModel.uiState.value.chatMessages.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun unreadableImage_reportsAnErrorWithoutInventingAnImageDescription() = runTest {
        repository.failImage = true
        viewModel.sendChatMessage("Help with this photo", "unreadable")
        advanceUntilIdle()
        assertEquals("Image unavailable", viewModel.uiState.value.error)
        assertEquals("", repository.chatText)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test fun generalSosDoesNotAssumeCardiacArrest_andReportedSymptomsReachTheAssistant() = runTest {
        viewModel.initialize("general_emergency", "incident-conversation")
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.protocol)
        viewModel.sendChatMessage("What can I do?", emergencyDescription = "Medical emergency: Seizure")
        advanceUntilIdle()
        assertTrue(repository.chatText.contains("Medical emergency: Seizure"))
        assertTrue(repository.chatText.contains("What can I do?"))
        assertEquals("What can I do?", viewModel.uiState.value.chatMessages.first().text)
        viewModel.initialize("general_emergency", "incident-conversation")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.chatMessages.size)
        assertEquals(1, repository.loads)
    }

    private class ProtocolRepository : IAiAgentRepository {
        var loads = 0
        var image = ""
        var chatText = ""
        var failImage = false
        override suspend fun describeImage(base64: String): Result<String> {
            image = base64
            return if (failImage) Result.failure(IllegalStateException("Image unavailable")) else Result.success("A first-aid document")
        }
        override suspend fun getAllProtocols(): List<GroundedProtocolDto> {
            loads++
            delay(100)
            return listOf(protocol("cardiac_arrest"), protocol("burns"))
        }
        override suspend fun getProtocol(conditionId: String) = protocol(conditionId)
        override suspend fun chatWithAgent(sessionId: String, text: String, currentStepIndex: Int, completedSteps: List<Int>): AgentChatResponseDto {
            chatText = text
            return AgentChatResponseDto(sessionId, "How can I help?", "", "GUIDANCE", "general_emergency", 1, "low", currentStepIndex, completedSteps, false, 0)
        }
        override suspend fun generateHandover(sessionId: String): ClinicalHandoverSummaryDto = error("Unused")
        private fun protocol(id: String) = GroundedProtocolDto(
            conditionId = id, conditionLabel = id, crisisType = "medical", severityLevel = 3,
            priority = "HIGH", protocolTitle = id, authority = "Test", disclaimers = "Test",
            legalShield = "Test", recommendedRadiusKm = 1.5, emergencyNumber = "108",
        )
    }
}
