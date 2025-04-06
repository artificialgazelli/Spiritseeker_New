package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.Dissertation
import com.example.spiritseeker.data.model.DissertationTask
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate // Import LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DissertationViewModel @Inject constructor(
    private val repository: DataRepository,
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    // Observe health status
    // Renamed healthSettings to appSettings
    private val appSettings = appSettingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) // Keep null default or use AppSettings()?

    // Flow for the dissertation data
    val dissertation: StateFlow<Dissertation?> = repository.getDissertation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Function to log hours worked on a specific task
    fun logHoursWorked(taskName: String, hours: Double) {
        viewModelScope.launch {
            val currentDissertation = dissertation.value ?: return@launch
            if (hours <= 0) return@launch

            var pointsEarned = (hours * 5).toInt() // Example: 5 points per hour
            if (appSettings.value?.healthStatus == false) pointsEarned = 0 // Check appSettings


            // Find the task and update its hours_worked
            val updatedTasks = currentDissertation.tasks?.let { tasks ->
                tasks.copy(
                    preparation = updateTaskHours(tasks.preparation, taskName, hours),
                    empirical = updateTaskHours(tasks.empirical, taskName, hours),
                    integration = updateTaskHours(tasks.integration, taskName, hours),
                    finalization = updateTaskHours(tasks.finalization, taskName, hours)
                )
            }

            if (updatedTasks != currentDissertation.tasks) { // Check if an update actually happened
                val updatedDissertation = currentDissertation.copy(
                    points = currentDissertation.points + pointsEarned,
                    tasks = updatedTasks,
                    lastPractice = repository.getCurrentDate() // Use repository function
                    // TODO: Implement streak logic if applicable (using repository.calculateNewStreak)
                    // TODO: Check for level up (using a calculateLevel function, maybe move to repo too?)
                )
                repository.updateDissertation(updatedDissertation)
                if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
            }
        }
    }

    // Helper function to update hours in a list of tasks
    private fun updateTaskHours(
        taskList: List<DissertationTask>,
        taskName: String,
        hoursToAdd: Double
    ): List<DissertationTask> {
        return taskList.map { task ->
            if (task.name == taskName) {
                task.copy(hoursWorked = task.hoursWorked + hoursToAdd)
            } else {
                task
            }
        }
    }

    // Removed getCurrentDate as it's now in DataRepository
}