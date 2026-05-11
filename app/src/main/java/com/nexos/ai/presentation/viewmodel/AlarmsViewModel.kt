package com.nexos.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexos.ai.data.local.entity.Alarm
import com.nexos.ai.data.repository.AlarmRepository
import com.nexos.ai.util.NaturalLanguageTimeParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmsUiState(
    val input: String = "",
    val parsePreview: NaturalLanguageTimeParser.Result? = null,
    val parseError: String? = null,
    val canScheduleExact: Boolean = true,
    val lastCreatedId: Long = -1L
)

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _state = MutableStateFlow(
        AlarmsUiState(canScheduleExact = repository.canScheduleExactAlarms())
    )
    val state: StateFlow<AlarmsUiState> = _state.asStateFlow()

    fun onInputChange(text: String) {
        val parsed = NaturalLanguageTimeParser.parse(text)
        _state.update {
            it.copy(
                input = text,
                parsePreview = parsed,
                parseError = if (text.isNotBlank() && parsed == null)
                    "Couldn't understand — try \"remind me at 8am tomorrow\""
                else null
            )
        }
    }

    fun refreshExactPermission() {
        _state.update { it.copy(canScheduleExact = repository.canScheduleExactAlarms()) }
    }

    fun submit() {
        val current = _state.value
        val parsed = current.parsePreview ?: return
        viewModelScope.launch {
            val alarm = Alarm(
                title = parsed.title,
                rawRequest = current.input,
                triggerAt = parsed.triggerAtMillis,
                isEnabled = true
            )
            val saved = repository.add(alarm)
            _state.update {
                AlarmsUiState(
                    canScheduleExact = repository.canScheduleExactAlarms(),
                    lastCreatedId = saved.id
                )
            }
        }
    }

    fun toggle(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch { repository.toggle(alarm, enabled) }
    }

    /**
     * Schedule a clock-style alarm: a specific hour/minute, optionally repeating on a
     * bitmask of weekdays. For a one-shot alarm (no recur), the first fire is the next
     * occurrence of that time (today if still in the future, otherwise tomorrow).
     */
    fun setClockAlarm(label: String, hour: Int, minute: Int, recurMask: Int) {
        viewModelScope.launch {
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                if (recurMask == 0 && timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }
            val cleaned = label.ifBlank { "Alarm" }
            repository.add(
                Alarm(
                    title = cleaned,
                    rawRequest = "%s at %02d:%02d".format(cleaned, hour, minute),
                    triggerAt = cal.timeInMillis,
                    isEnabled = true,
                    recurDaysMask = recurMask
                )
            )
        }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch { repository.delete(alarm) }
    }
}
