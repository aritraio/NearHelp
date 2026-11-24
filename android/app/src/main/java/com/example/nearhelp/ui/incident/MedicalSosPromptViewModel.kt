package com.example.nearhelp.ui.incident

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val GENERIC_MEDICAL_SOS = "Medical emergency. I need urgent medical help at my live location."
const val MEDICAL_PROMPT_DURATION_MILLIS = 5_000L

data class MedicalPromptState(val remainingMillis: Long = MEDICAL_PROMPT_DURATION_MILLIS, val description: String? = null, val claimed: Boolean = false, val cancelled: Boolean = false)

/** Deadline and dispatch claim survive rotation. A timed-out draft is never treated as submitted. */
class MedicalSosPromptViewModel(
    private val saved: SavedStateHandle = SavedStateHandle(),
    private val elapsedTime: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {
    private val _state = MutableStateFlow(MedicalPromptState(description = saved["description"], claimed = saved["claimed"] ?: false, cancelled = saved["cancelled"] ?: false))
    val state = _state.asStateFlow()
    private var timer: Job? = null

    fun start() {
        if (timer?.isActive == true || _state.value.description != null || _state.value.cancelled) return
        val deadline = saved.get<Long>("deadline") ?: (elapsedTime() + MEDICAL_PROMPT_DURATION_MILLIS).also { saved["deadline"] = it }
        timer = viewModelScope.launch {
            while (_state.value.description == null && !_state.value.cancelled) {
                val remaining = (deadline - elapsedTime()).coerceAtLeast(0)
                _state.value = _state.value.copy(remainingMillis = remaining)
                if (remaining == 0L) { submit(""); break }
                delay(minOf(100L, remaining))
            }
        }
    }

    fun submit(text: String) {
        if (_state.value.description != null || _state.value.cancelled) return
        val description = text.trim().take(180).takeIf { it.isNotBlank() }?.let { "Medical emergency: $it" } ?: GENERIC_MEDICAL_SOS
        saved["description"] = description
        _state.value = _state.value.copy(description = description, remainingMillis = 0)
        timer?.cancel()
    }

    fun claim(): String? {
        val current = _state.value
        if (current.cancelled || current.claimed || current.description == null) return null
        saved["claimed"] = true
        _state.value = current.copy(claimed = true)
        return current.description
    }

    fun cancel() {
        if (_state.value.claimed) return
        saved["cancelled"] = true
        _state.value = _state.value.copy(cancelled = true)
        timer?.cancel()
    }
}
