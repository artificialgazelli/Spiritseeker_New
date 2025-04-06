package com.example.spiritseeker.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.AppSettings // Import AppSettings
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsManager: AppSettingsManager, // Inject AppSettingsManager
    private val repository: DataRepository
) : ViewModel() {

    // --- App Settings (includes Pomodoro, Health, Points) ---
    val appSettings: StateFlow&lt;AppSettings&gt; = appSettingsManager.settingsFlow // Use appSettingsManager
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()) // Use AppSettings default

    // Update only the Pomodoro parts of AppSettings
    fun updatePomodoroSettings(updatedPomodoro: AppSettings) {
        viewModelScope.launch {
            // Create a new AppSettings object preserving non-pomodoro values
            val currentSettings = appSettings.value // Correct variable name
            val newSettings = currentSettings.copy(
                duration = updatedPomodoro.duration,
                shortBreak = updatedPomodoro.shortBreak,
                longBreak = updatedPomodoro.longBreak,
                sessionsBeforeLongBreak = updatedPomodoro.sessionsBeforeLongBreak,
                autoStartBreaks = updatedPomodoro.autoStartBreaks,
                autoStartPomodoros = updatedPomodoro.autoStartPomodoros
            )
            appSettingsManager.updatePomodoroSettings(newSettings) // Call specific update function if available, or a general one
        }
    }

    // --- Data Management ---
    // Use StateFlows to communicate results/status of operations back to UI if needed
    private val _dataOperationStatus = MutableStateFlow<String?>(null)
    val dataOperationStatus: StateFlow<String?> = _dataOperationStatus.asStateFlow()

    fun resetDataOperationStatus() {
        _dataOperationStatus.value = null
    }

    // Triggered after user selects a directory via SAF (ACTION_OPEN_DOCUMENT_TREE)
    fun backupData(targetDirectoryUri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = repository.backupData(targetDirectoryUri)
                if (fileName != null) {
                     _dataOperationStatus.value = "Backup successful: $fileName"
                } else {
                     _dataOperationStatus.value = "Backup failed."
                }
            } catch (e: Exception) {
                _dataOperationStatus.value = "Backup failed: ${e.message}"
            }
        }
    }

    // Triggered after user selects a backup file via SAF (ACTION_OPEN_DOCUMENT)
    fun restoreData(backupFileUri: Uri) {
         viewModelScope.launch {
            try {
                val success = repository.restoreData(backupFileUri)
                if (success) {
                    _dataOperationStatus.value = "Restore successful."
                    // Data is reloaded automatically by flows, UI should update.
                } else {
                     _dataOperationStatus.value = "Restore failed. Invalid file?"
                }
            } catch (e: Exception) {
                _dataOperationStatus.value = "Restore failed: ${e.message}"
            }
        }
    }

     fun resetData() {
        viewModelScope.launch {
            try {
                repository.resetAllData() // This deletes and re-initializes defaults
                _dataOperationStatus.value = "Data reset successfully."
            } catch (e: Exception) {
                _dataOperationStatus.value = "Data reset failed: ${e.message}"
            }
        }
    }

    // --- Theme Settings (Placeholder) ---
    // TODO: Implement theme switching logic if desired
}