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

    private class ProtocolRepository : IAiAgentRepository {
        var loads = 0
        override suspend fun getAllProtocols(): List<GroundedProtocolDto> {
            loads++
            delay(100)
            return listOf(protocol("cardiac_arrest"), protocol("burns"))
        }
        override suspend fun getProtocol(conditionId: String) = protocol(conditionId)
        override suspend fun chatWithAgent(sessionId: String, text: String, currentStepIndex: Int, completedSteps: List<Int>): AgentChatResponseDto = error("Unused")
        override suspend fun generateHandover(sessionId: String): ClinicalHandoverSummaryDto = error("Unused")
        private fun protocol(id: String) = GroundedProtocolDto(
            conditionId = id, conditionLabel = id, crisisType = "medical", severityLevel = 3,
            priority = "HIGH", protocolTitle = id, authority = "Test", disclaimers = "Test",
            legalShield = "Test", recommendedRadiusKm = 1.5, emergencyNumber = "108",
        )
    }
}
