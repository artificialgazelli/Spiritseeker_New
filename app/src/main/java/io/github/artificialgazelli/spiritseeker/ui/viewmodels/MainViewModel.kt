package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.AppSettings // Import AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    // Renamed healthSettings to appSettings and corrected type/manager/default
    val appSettings: StateFlow&lt;AppSettings&gt; = appSettingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _showHealthCheckDialog = MutableStateFlow(false)
    val showHealthCheckDialog: StateFlow<Boolean> = _showHealthCheckDialog.asStateFlow()

    init {
        // Check if health check needs to be shown on ViewModel initialization
        viewModelScope.launch {
            appSettings.collectLatest { settings -> // Observe appSettings
                val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                if (settings.lastHealthCheck != todayStr) {
                    // Only show if not already shown or dismissed for today (add more state if needed)
                    _showHealthCheckDialog.value = true
                } else {
                    _showHealthCheckDialog.value = false // Ensure it's hidden if already done
                }
            }
        }
    }

    fun submitHealthCheck(eatingWell: Boolean, exercised: Boolean, mentalHealth: Boolean) {
        val overallStatus = eatingWell && exercised && mentalHealth
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        viewModelScope.launch {
            appSettingsManager.updateHealthCheck(overallStatus, todayStr) // Call AppSettingsManager
        }
        _showHealthCheckDialog.value = false // Hide dialog after submission
        // TODO: Show confirmation Snackbar/Toast?
    }

    fun dismissHealthCheckDialog() {
        _showHealthCheckDialog.value = false
        // Optionally, store a flag in preferences/memory to not show again today if dismissed
    }
}