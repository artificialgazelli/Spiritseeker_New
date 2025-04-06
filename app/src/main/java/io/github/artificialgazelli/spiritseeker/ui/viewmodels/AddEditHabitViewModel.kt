package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.AppDestinations
import com.example.spiritseeker.data.model.Habit
import com.example.spiritseeker.data.model.HabitCategory
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val repository: DataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitName: String? = savedStateHandle.get<String>(AppDestinations.HABIT_NAME_ARG)
    val isEditing = habitName != null

    private val _habitState = MutableStateFlow<Habit?>(null)
    val habitState: StateFlow<Habit?> = _habitState.asStateFlow()

    val categories: StateFlow<List<HabitCategory>> = repository.getAllHabitCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for UI fields (consider a separate UI state data class for complex forms)
    val habitNameInput = MutableStateFlow("")
    val iconInput = MutableStateFlow("❓") // Default icon
    val categoryInput = MutableStateFlow<String?>(null)
    val frequencyInput = MutableStateFlow("daily") // Default frequency
    val intervalInput = MutableStateFlow("2") // Default interval
    val specificDaysInput = MutableStateFlow<Set<Int>>(emptySet()) // For weekly

    init {
        if (isEditing && habitName != null) {
            loadHabitData(habitName)
        }
    }

    private fun loadHabitData(name: String) {
        viewModelScope.launch {
            repository.getHabitByName(name).collectLatest { habit ->
                _habitState.value = habit
                habit?.let {
                    habitNameInput.value = it.name
                    iconInput.value = it.icon
                    categoryInput.value = it.category
                    frequencyInput.value = it.frequency
                    intervalInput.value = it.interval?.toString() ?: "2"
                    specificDaysInput.value = it.specificDays?.toSet() ?: emptySet()
                }
            }
        }
    }

    fun saveHabit() {
        viewModelScope.launch {
            val name = habitNameInput.value.trim()
            if (name.isEmpty()) {
                // TODO: Show error message to user
                return@launch
            }

            val habitToSave = Habit(
                name = name,
                icon = iconInput.value,
                active = _habitState.value?.active ?: true, // Keep existing active state or default to true
                isCustom = true, // Assume habits added/edited via UI are custom
                category = categoryInput.value,
                frequency = frequencyInput.value,
                interval = if (frequencyInput.value == "interval") intervalInput.value.toIntOrNull() else null,
                specificDays = if (frequencyInput.value == "weekly") specificDaysInput.value.toList().sorted() else null,
                streak = _habitState.value?.streak ?: 0, // Preserve existing streak
                completedDates = _habitState.value?.completedDates ?: emptyList() // Preserve existing dates
            )

            // Use insert which acts as upsert due to OnConflictStrategy.REPLACE
            repository.insertHabit(habitToSave)
            // TODO: Add navigation back or success message handling
        }
    }

    fun updateSpecificDays(dayIndex: Int, isSelected: Boolean) {
        val currentDays = specificDaysInput.value.toMutableSet()
        if (isSelected) {
            currentDays.add(dayIndex)
        } else {
            currentDays.remove(dayIndex)
        }
        specificDaysInput.value = currentDays
    }
}