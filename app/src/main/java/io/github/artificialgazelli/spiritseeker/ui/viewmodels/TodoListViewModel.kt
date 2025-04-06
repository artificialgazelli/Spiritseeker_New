package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.*
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.UUID // Import UUID
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: DataRepository,
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    // Observe health status
    private val appSettings = appSettingsManager.settingsFlow // Use AppSettingsManager
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) // Keep null default or use AppSettings()?

    // Could potentially add filters (e.g., show completed, filter by group)
    private val _allTasks = repository.getAllTasks() // Keep private source

    // State for filtering
    private val _selectedGroupFilter = MutableStateFlow&lt;String?&gt;(null) // null means "All"
    val selectedGroupFilter: StateFlow&lt;String?&gt; = _selectedGroupFilter.asStateFlow()

    // Filtered tasks based on selection
    val filteredTasks: StateFlow&lt;List&lt;TodoTask&gt;&gt; = combine(
        _allTasks,
        _selectedGroupFilter
    ) { tasks, group ->
        if (group == null) {
            tasks // No filter
        } else {
            tasks.filter { it.group == group }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<TodoGroup>> = repository.getAllTodoGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Example function to add a new task
    fun addTask(task: TodoTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    // Example function to update an existing task
    fun updateTask(task: TodoTask) {
         viewModelScope.launch {
            repository.updateTask(task)
        }
    }
    
        // --- Recurrence Calculation Logic ---
        private fun calculateNextRecurrence(completedTask: TodoTask): TodoTask? {
            val recurrence = completedTask.recurrence ?: return null
            val lastDueDate = completedTask.dueDate?.let { LocalDate.parse(it) } ?: LocalDate.now() // Base off due date or today
    
            var nextDueDate = when (recurrence.type) {
                "daily" -> lastDueDate.plusDays(recurrence.interval.toLong())
                "weekly" -> calculateNextWeeklyDueDate(lastDueDate, recurrence.interval, recurrence.days)
                "monthly" -> lastDueDate.plusMonths(recurrence.interval.toLong())
                "yearly" -> lastDueDate.plusYears(recurrence.interval.toLong())
                else -> return null // Unknown type
            }
    
            // Adjust for weekly specific days if needed after interval jump
            if (recurrence.type == "weekly" && !recurrence.days.isNullOrEmpty()) {
                 nextDueDate = findNextValidWeekday(nextDueDate, recurrence.days)
            }
    
    
            // Check against end date
            recurrence.endDate?.let {
                val end = LocalDate.parse(it)
                if (nextDueDate.isAfter(end)) {
                    return null // Stop recurrence
                }
            }
    
            // Create the new task instance
            return completedTask.copy(
                id = UUID.randomUUID().toString(), // New ID for the next instance
                completed = false,
                completionDate = null,
                dueDate = nextDueDate.format(DateTimeFormatter.ISO_DATE)
                // Keep other fields like name, group, priority, notes, recurrence rule
            )
        }
    
        private fun calculateNextWeeklyDueDate(lastDueDate: LocalDate, interval: Int, days: List&lt;Int&gt;?): LocalDate {
             // Simply jump by weeks first
             return lastDueDate.plusWeeks(interval.toLong())
             // findNextValidWeekday will handle finding the correct day in that week or later
        }
    
         private fun findNextValidWeekday(startDate: LocalDate, validDaysOfWeek: List&lt;Int&gt;): LocalDate {
            if (validDaysOfWeek.isNullOrEmpty()) return startDate // No specific days, return date as is
    
            var nextDate = startDate
            val validDays = validDaysOfWeek.map { DayOfWeek.of((it % 7) + 1) } // Convert 0-6 to DayOfWeek enum
    
            // Find the first valid day on or after the calculated start date
            while (nextDate.dayOfWeek !in validDays) {
                nextDate = nextDate.plusDays(1)
            }
            return nextDate
        }

    // Function to toggle completion (copied from ModulesViewModel for consistency)
    fun toggleTaskCompletion(task: TodoTask) {
         viewModelScope.launch {
             val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
             val updatedTask = if (task.completed) {
                 // Un-complete
                 task.copy(completed = false, completionDate = null)
             } else {
                 // Complete
                 val pointsAwarded = when (task.priority.lowercase()) {
                     "high" -> 10
                     "medium" -> 5
                     "low" -> 2
                     else -> 1
                 }
                 if (appSettings.value?.healthStatus == true) {
                    appSettingsManager.addTotalPoints(pointsAwarded) // Add points
                    // println("Awarded $pointsAwarded points for completing task: ${task.name}") // Can remove print
                 } else {
                     println("Health check not complete, no points awarded for task: ${task.name}") // Keep feedback
                 }
                 // TODO: Add points to a central pool or relevant skill
                 val completedTask = task.copy(completed = true, completionDate = todayStr)
                 // Handle recurrence
                 val nextRecurrence = calculateNextRecurrence(completedTask)
                 if (nextRecurrence != null) {
                     // Insert the next instance
                     repository.insertTask(nextRecurrence)
                     // Keep the completed instance (or delete/archive based on preference)
                 }
                 completedTask // Return the updated original task
             }
             repository.updateTask(updatedTask)
         }
     }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteTaskById(task.id) // Assuming delete by ID exists
        }
    }
    // TODO: Add functions for managing groups

    fun setGroupFilter(groupName: String?) {
        _selectedGroupFilter.value = groupName
    }
}