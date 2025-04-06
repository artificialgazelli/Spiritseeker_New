package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.AppDestinations
import com.example.spiritseeker.data.model.Recurrence // Import Recurrence
import com.example.spiritseeker.data.model.TodoGroup
import com.example.spiritseeker.data.model.TodoTask
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: DataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String? = savedStateHandle.get<String>(AppDestinations.TASK_ID_ARG)
    val isEditing = taskId != null

    private val _taskState = MutableStateFlow<TodoTask?>(null)
    // val taskState: StateFlow<TodoTask?> = _taskState.asStateFlow() // Expose if needed

    val groups: StateFlow<List<TodoGroup>> = repository.getAllTodoGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI State ---
    val taskNameInput = MutableStateFlow("")
    val groupInput = MutableStateFlow("Personal") // Default group
    val priorityInput = MutableStateFlow("Medium") // Default priority
    val dueDateInput = MutableStateFlow<LocalDate?>(null) // Store as LocalDate for easier handling
    val notesInput = MutableStateFlow("")
    // TODO: Add state for recurrence UI

    init {
        if (isEditing && taskId != null) {
            loadTaskData(taskId)
        }
    }

    private fun loadTaskData(id: String) {
        viewModelScope.launch {
            repository.getTaskById(id).collectLatest { task ->
                _taskState.value = task
                task?.let {
                    taskNameInput.value = it.name
                    groupInput.value = it.group
                    priorityInput.value = it.priority
                    dueDateInput.value = it.dueDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
                    notesInput.value = it.notes ?: ""
                    // Load recurrence state
                    it.recurrence?.let { rec ->
                        recurrenceEnabled.value = true
                        recurrenceType.value = rec.type
                        recurrenceInterval.value = rec.interval.toString()
                        recurrenceDaysOfWeek.value = rec.days?.toSet() ?: emptySet()
                        recurrenceEndDate.value = rec.endDate?.let { LocalDate.parse(it) }
                    } ?: run {
                        recurrenceEnabled.value = false // Ensure disabled if no recurrence data
                    }
                }
            }
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            val name = taskNameInput.value.trim()
            if (name.isEmpty()) {
                // TODO: Show error
                return@launch
            }

            val taskToSave = TodoTask(
                id = taskId ?: UUID.randomUUID().toString(), // Use existing ID if editing, else generate new
                name = name,
                group = groupInput.value,
                priority = priorityInput.value,
                dueDate = dueDateInput.value?.format(DateTimeFormatter.ISO_DATE),
                notes = notesInput.value.trim().takeIf { it.isNotEmpty() },
                completed = _taskState.value?.completed ?: false, // Preserve existing state
                completionDate = _taskState.value?.completionDate, // Preserve existing state
                recurrence = if (recurrenceEnabled.value) {
                    Recurrence(
                        type = recurrenceType.value,
                        interval = recurrenceInterval.value.toIntOrNull() ?: 1,
                        days = if (recurrenceType.value == "weekly") recurrenceDaysOfWeek.value.toList().sorted() else null,
                        endDate = recurrenceEndDate.value?.format(DateTimeFormatter.ISO_DATE)
                    )
                } else {
                    null
                }
            )

            repository.insertTask(taskToSave) // insert acts as upsert
             // TODO: Add navigation back or success message handling
        }
    }

     fun updateDueDate(date: LocalDate?) {
        dueDateInput.value = date
    }

    fun updateRecurrenceEndDate(date: LocalDate?) {
        recurrenceEndDate.value = date
    }

     fun updateRecurrenceDays(dayIndex: Int, isSelected: Boolean) {
        val currentDays = recurrenceDaysOfWeek.value.toMutableSet()
        if (isSelected) {
            currentDays.add(dayIndex)
        } else {
            currentDays.remove(dayIndex)
        }
        recurrenceDaysOfWeek.value = currentDays
    }
}