package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.AppSettings // Import AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class PomodoroState {
    IDLE, // Timer stopped or reset
    RUNNING, // Pomodoro session running
    PAUSED, // Timer paused
    SHORT_BREAK, // Short break running
    LONG_BREAK // Long break running
}

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    // Observe settings from AppSettingsManager
    val settings: StateFlow&lt;AppSettings&gt; = appSettingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()) // Default AppSettings
    // Removed duplicate settings declaration

    private val _timerState = MutableStateFlow(PomodoroState.IDLE)
    val timerState: StateFlow<PomodoroState> = _timerState.asStateFlow()

    private val _remainingTimeMillis = MutableStateFlow(0L)
    val remainingTimeMillis: StateFlow<Long> = _remainingTimeMillis.asStateFlow()

    private val _completedSessions = MutableStateFlow(0)
    val completedSessions: StateFlow<Int> = _completedSessions.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Reset timer if settings change while idle (already observing settings flow)
        viewModelScope.launch {
            settings.drop(1).collect { // drop(1) to ignore initial value
                 if (_timerState.value == PomodoroState.IDLE) {
                    resetTimer() // Reset timer based on new settings
                }
            }
        }
    }

    fun startTimer() {
        if (_timerState.value == PomodoroState.IDLE || _timerState.value == PomodoroState.PAUSED) {
            val durationMillis = if (_timerState.value == PomodoroState.PAUSED) {
                _remainingTimeMillis.value // Resume from paused time
            } else {
                 // Start new session based on current state (or default to pomodoro)
                determineNextDurationMillis()
            }
             _timerState.value = determineNextRunningState()
            startCountdown(durationMillis)
        }
    }

     fun pauseTimer() {
        if (_timerState.value == PomodoroState.RUNNING ||
            _timerState.value == PomodoroState.SHORT_BREAK ||
            _timerState.value == PomodoroState.LONG_BREAK) {
            timerJob?.cancel()
            _timerState.value = PomodoroState.PAUSED
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = PomodoroState.IDLE
        _remainingTimeMillis.value = settings.value.duration * 60 * 1000L // Reset to Pomodoro duration
        // Optionally reset completed sessions: _completedSessions.value = 0
    }

    fun skipTimer() {
         timerJob?.cancel()
         handleTimerFinish() // Directly transition to the next state
    }


    private fun startCountdown(durationMillis: Long) {
        _remainingTimeMillis.value = durationMillis
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            var currentTime = durationMillis
            while (currentTime > 0 && isActive) {
                delay(1000) // Update every second
                currentTime -= 1000
                _remainingTimeMillis.value = currentTime
            }
            // Timer finished
            if (isActive) { // Ensure job wasn't cancelled
                 withContext(Dispatchers.Main) {
                    handleTimerFinish()
                }
            }
        }
    }

    private fun handleTimerFinish() {
        val currentState = _timerState.value
        val currentSettings = settings.value

        when (currentState) {
            PomodoroState.RUNNING -> {
                _completedSessions.value++
                // Determine if it's time for a long break
                val nextState = if (_completedSessions.value % currentSettings.sessionsBeforeLongBreak == 0) {
                    PomodoroState.LONG_BREAK
                } else {
                    PomodoroState.SHORT_BREAK
                }
                _timerState.value = nextState
                _remainingTimeMillis.value = determineNextDurationMillis(nextState)

                // Auto-start break if enabled
                if (currentSettings.autoStartBreaks) {
                    startTimer()
                } else {
                     _timerState.value = PomodoroState.IDLE // Set to idle, ready for manual start
                }
            }
            PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> {
                 _timerState.value = PomodoroState.RUNNING // Back to Pomodoro
                 _remainingTimeMillis.value = determineNextDurationMillis(PomodoroState.RUNNING)

                 // Auto-start next pomodoro if enabled
                 if (currentSettings.autoStartPomodoros) {
                     startTimer()
                 } else {
                      _timerState.value = PomodoroState.IDLE // Set to idle, ready for manual start
                 }
            }
            else -> { // IDLE, PAUSED - Should not happen here, but reset just in case
                 resetTimer()
            }
        }
         // TODO: Add logic to award points/log completion for Pomodoro session if needed
    }

     private fun determineNextDurationMillis(nextState: PomodoroState? = null): Long {
        val state = nextState ?: _timerState.value // Use provided state or current state
        val currentSettings = settings.value
        return when (state) {
            PomodoroState.RUNNING, PomodoroState.IDLE, PomodoroState.PAUSED -> currentSettings.duration * 60 * 1000L
            PomodoroState.SHORT_BREAK -> currentSettings.shortBreak * 60 * 1000L
            PomodoroState.LONG_BREAK -> currentSettings.longBreak * 60 * 1000L
        }
    }

     private fun determineNextRunningState(): PomodoroState {
         return when (_timerState.value) {
             PomodoroState.IDLE -> PomodoroState.RUNNING // Default start is Pomodoro
             PomodoroState.PAUSED -> {
                 // Determine what was running before pause - tricky without storing previous state
                 // Assume resuming the intended next state based on completed sessions
                 if (_completedSessions.value % settings.value.sessionsBeforeLongBreak == 0 && _completedSessions.value > 0) {
                     PomodoroState.LONG_BREAK
                 } else if (_completedSessions.value > 0) {
                      PomodoroState.SHORT_BREAK
                 } else {
                      PomodoroState.RUNNING
                 }
                 // This logic might need refinement based on exact desired resume behavior
             }
             else -> PomodoroState.RUNNING // Should not happen, default to RUNNING
         }
     }


    // --- Settings Update ---
    // updatePomodoroSettings is now handled in SettingsViewModel
    /* fun updatePomodoroSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            appSettingsManager.updatePomodoroSettings(newSettings) // Call new manager
            // Reset timer is handled by the flow collection in init
        }
    } */

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel() // Ensure timer stops when ViewModel is destroyed
    }
}