package com.example.nearhelp.ui.incident

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicalSosPromptViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun noSubmission_sendsGenericOnlyAtFourSeconds() = runTest {
        val vm = MedicalSosPromptViewModel(elapsedTime = { testScheduler.currentTime })
        vm.start(); runCurrent(); advanceTimeBy(3999); runCurrent()
        assertNull(vm.state.value.description)
        advanceTimeBy(1); runCurrent()
        assertEquals(GENERIC_MEDICAL_SOS, vm.state.value.description)
        assertEquals(GENERIC_MEDICAL_SOS, vm.claim())
        assertNull(vm.claim())
    }

    @Test fun selectedSymptom_cancelsFallbackAndCanOnlyBeDispatchedOnce() = runTest {
        val vm = MedicalSosPromptViewModel(elapsedTime = { testScheduler.currentTime })
        vm.start(); runCurrent(); advanceTimeBy(3900); runCurrent()
        vm.submit("Breathing trouble")
        val sent = vm.claim()
        advanceTimeBy(5000); runCurrent()
        assertEquals("Medical emergency: Breathing trouble", sent)
        assertEquals(sent, vm.state.value.description)
        assertNull(vm.claim())
        vm.submit("Chest pain")
        assertEquals(sent, vm.state.value.description)
    }

    @Test fun cancellation_preventsAutomaticAndManualDispatch() = runTest {
        val vm = MedicalSosPromptViewModel(elapsedTime = { testScheduler.currentTime })
        vm.start(); runCurrent(); advanceTimeBy(1000); vm.cancel()
        advanceTimeBy(5000); runCurrent(); vm.submit("Chest pain")
        assertNull(vm.state.value.description)
        assertNull(vm.claim())
        assertTrue(vm.state.value.cancelled)
    }

    @Test fun restoredDeadline_doesNotGiveAnotherFourSeconds() = runTest {
        val saved = SavedStateHandle(mapOf("deadline" to 4000L))
        val vm = MedicalSosPromptViewModel(saved, elapsedTime = { testScheduler.currentTime + 3000 })
        vm.start(); runCurrent(); advanceTimeBy(999); runCurrent()
        assertNull(vm.state.value.description)
        advanceTimeBy(1); runCurrent()
        assertEquals(GENERIC_MEDICAL_SOS, vm.state.value.description)
    }

    @Test fun savedSubmissionAndClaim_areNotReplayedAfterRecreation() = runTest {
        val saved = SavedStateHandle()
        val first = MedicalSosPromptViewModel(saved, elapsedTime = { testScheduler.currentTime })
        first.submit("Seizure"); assertNotNull(first.claim())
        val restored = MedicalSosPromptViewModel(saved, elapsedTime = { testScheduler.currentTime })
        restored.start(); advanceUntilIdle()
        assertEquals("Medical emergency: Seizure", restored.state.value.description)
        assertNull(restored.claim())
    }
}
