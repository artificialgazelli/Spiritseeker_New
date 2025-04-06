package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.Habit
import com.example.spiritseeker.data.model.HabitCategory
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HabitTrackerViewModel @Inject constructor(
    private val repository: DataRepository,
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    // Observe health status
    private val appSettings = appSettingsManager.settingsFlow // Use AppSettingsManager
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) // Keep null default or use AppSettings()?

    private val _allHabits = repository.getAllHabits() // Keep private source

    // State for filtering
    private val _selectedCategoryFilter = MutableStateFlow&lt;String?&gt;(null) // null means "All"
    val selectedCategoryFilter: StateFlow&lt;String?&gt; = _selectedCategoryFilter.asStateFlow()

    // Filtered habits based on selection
    val filteredHabits: StateFlow&lt;List&lt;Habit&gt;&gt; = combine(
        _allHabits,
        _selectedCategoryFilter
    ) { habits, category ->
        if (category == null) {
            habits // No filter applied
        } else {
            habits.filter { it.category == category }
        }
    }.map { habits -> // Add map operation for streak check
        habits.map { checkAndResetHabitStreakIfNeeded(it) } // Check each habit
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<HabitCategory>> = repository.getAllHabitCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Example function to add a new habit
    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    // Example function to update an existing habit
    fun updateHabit(habit: Habit) {
         viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

     // Function to toggle completion (copied from ModulesViewModel for consistency,
     // could be refactored into Repository or a UseCase later)
    fun toggleHabitCompletion(habit: Habit) {
        val pointsPerHabit = 5 // Define points awarded per completion
        viewModelScope.launch {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val currentCompletedDates = habit.completedDates.toMutableList()
            val currentStreak = habit.streak
            var newStreak = currentStreak // Needs proper calculation based on consecutive days

            val updatedHabit = if (currentCompletedDates.contains(todayStr)) {
                // Un-complete
                currentCompletedDates.remove(todayStr)
                // TODO: Implement proper streak calculation (decrement if today was last completed day)
                newStreak = if (currentStreak > 0) currentStreak - 1 else 0 // Simplistic decrement
                habit.copy(completedDates = currentCompletedDates, streak = newStreak)
            } else {
                // Complete
                currentCompletedDates.add(todayStr)
                 // TODO: Implement proper streak calculation (check yesterday's completion)
                newStreak = currentStreak + 1 // Simplistic increment
                // Award points - Note: This doesn't add to a central point pool yet.
                // We might need a separate mechanism or add points to a related skill/general pool.
                // For now, just logging the concept.
                if (appSettings.value?.healthStatus == true) {
                    appSettingsManager.addTotalPoints(pointsPerHabit) // Add points
                    // println("Awarded $pointsPerHabit points for completing habit: ${habit.name}") // Can remove print
                } else {
                     println("Health check not complete, no points awarded for habit: ${habit.name}") // Keep feedback for no points
                }
                habit.copy(completedDates = currentCompletedDates, streak = newStreak)
            }
            repository.updateHabit(updatedHabit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabitByName(habit.name) // Assuming delete by name exists in repo/DAO
        }
    }
    // TODO: Add functions for managing categories

    fun setCategoryFilter(categoryName: String?) {
        _selectedCategoryFilter.value = categoryName
    }

    // --- Streak Check Logic for Habits ---
    // Note: This reads the last completed date, not a general 'lastPractice' like skills
    private suspend fun checkAndResetHabitStreakIfNeeded(habit: Habit): Habit {
        val today = LocalDate.now()
        val lastCompletionDate = habit.completedDates.maxOrNull()?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }

        if (lastCompletionDate != null && lastCompletionDate.isBefore(today.minusDays(1))) {
             // Streak is broken (last completion was before yesterday)
             if (habit.streak > 0) {
                 val updatedHabit = habit.copy(streak = 0)
                 repository.updateHabit(updatedHabit) // Update DB
                 return updatedHabit // Return updated habit for the flow
             }
        }
        // Also reset streak if last completion was today but user unchecks it?
        // Current toggle logic handles basic decrement, but doesn't check history.
        // More robust streak logic might be needed depending on exact requirements.
        return habit // Return original habit if streak is okay or already 0
    }
}