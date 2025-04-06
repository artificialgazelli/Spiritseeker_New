package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.model.*
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ModulesViewModel @Inject constructor(
    private val repository: DataRepository
) : ViewModel() {

    // --- StateFlows for UI ---
    private val _skills = MutableStateFlow<Map<String, Skill>>(emptyMap())
    val skills: StateFlow<Map<String, Skill>> = _skills.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _tasks = MutableStateFlow<List<TodoTask>>(emptyList())
    val tasks: StateFlow<List<TodoTask>> = _tasks.asStateFlow()

    // Combined state for today's relevant items
    val todaysHabits: StateFlow<List<Habit>> = habits
        .map { filterAndSortTodaysHabits(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaysTasks: StateFlow<List<TodoTask>> = tasks
        .map { filterAndSortTodaysTasks(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        // Observe data changes from the repository
        viewModelScope.launch {
            repository.getAllSkills().collect { skillList ->
                _skills.value = skillList.associateBy { it.name }
            }
        }
        viewModelScope.launch {
            repository.getAllHabits().collect { habitList ->
                _habits.value = habitList
            }
        }
        viewModelScope.launch {
            // Fetch only incomplete tasks for the dashboard initially
            repository.getIncompleteTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }

        // Initialize default data if the database is empty
        viewModelScope.launch { repository.initializeDefaultDataIfNeeded() }
    }

    // --- Helper Functions for Filtering/Sorting ---

    private fun filterAndSortTodaysHabits(allHabits: List<Habit>): List<Habit> {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_DATE) // YYYY-MM-DD

        return allHabits.filter { habit ->
            habit.active && when (habit.frequency) {
                "daily" -> true
                "weekly" -> habit.specificDays?.contains(today.dayOfWeek.value % 7) ?: false // Adjust Sunday=0
                "interval" -> {
                    // Basic interval check (needs refinement based on start date if available)
                    // For simplicity, show if not completed today
                    !habit.completedDates.contains(todayStr)
                    // A more robust check would involve calculating days since a start date or last completion
                }
                else -> false
            }
        }.sortedBy { it.completedDates.contains(todayStr) } // Incomplete first
    }

     private fun filterAndSortTodaysTasks(allTasks: List<TodoTask>): List<TodoTask> {
         val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
         // Show tasks due today or overdue and not completed
         return allTasks.filter { task ->
             !task.completed && (task.dueDate == null || task.dueDate <= todayStr)
         }
         // Sorting can be complex (priority, due date), keep simple for now
         .sortedWith(compareBy( { it.dueDate ?: "9999-99-99" }, { getPriorityOrder(it.priority) }))
     }

    private fun getPriorityOrder(priority: String): Int {
        return when (priority.lowercase()) {
            "high" -> 1
            "medium" -> 2
            "low" -> 3
            else -> 4
        }
    }


    // --- Event Handlers (will be called from UI) ---

    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val currentCompletedDates = habit.completedDates.toMutableList()
            val currentStreak = habit.streak
            var newStreak = currentStreak

            val updatedHabit = if (currentCompletedDates.contains(todayStr)) {
                // Un-complete
                currentCompletedDates.remove(todayStr)
                // Simple streak decrement (can be more complex)
                newStreak = if (currentStreak > 0) currentStreak - 1 else 0
                habit.copy(completedDates = currentCompletedDates, streak = newStreak)
            } else {
                // Complete
                currentCompletedDates.add(todayStr)
                 // Simple streak increment (needs logic for consecutive days)
                newStreak = currentStreak + 1 // Basic increment, needs proper check
                habit.copy(completedDates = currentCompletedDates, streak = newStreak)
                // TODO: Add logic to award points for habit completion
            }
            repository.updateHabit(updatedHabit)
        }
    }

     fun toggleTaskCompletion(task: TodoTask) {
         viewModelScope.launch {
             val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
             val updatedTask = if (task.completed) {
                 task.copy(completed = false, completionDate = null)
             } else {
                 task.copy(completed = true, completionDate = todayStr)
                 // TODO: Add logic to award points for task completion if applicable
             }
             repository.updateTask(updatedTask)
         }
     }

    // --- Navigation Actions (Placeholder) ---
    fun onOpenSkillQuest(skillName: String) {
        // TODO: Implement navigation to specific skill screen
        println("Navigate to $skillName quest")
    }

    fun onOpenHabitTracker() {
        // TODO: Implement navigation to full habit tracker screen
        println("Navigate to Habit Tracker")
    }

    fun onOpenTodoList() {
        // TODO: Implement navigation to full ToDo list screen
         println("Navigate to ToDo List")
    }

    fun onAddNewTask() {
        // TODO: Implement navigation/dialog to add new task
        println("Add New Task")
    }

    fun onAddNewHabit() {
        // TODO: Implement navigation/dialog to add new habit
        println("Add New Habit")
    }
}