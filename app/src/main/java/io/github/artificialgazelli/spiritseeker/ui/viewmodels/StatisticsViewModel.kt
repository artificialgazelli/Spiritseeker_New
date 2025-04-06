package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.*
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Data class to hold calculated statistics
data class AppStatistics(
    val totalPoints: Int = 0,
    val skillStats: Map<String, SkillStat> = emptyMap(),
    val habitCompletionRate: Float = 0f, // Overall completion rate for today/week?
    val tasksCompleted: Int = 0,
    val tasksPending: Int = 0,
    val generalPoints: Int = 0 // Add field for points from AppSettings
    // Add more stats as needed
)

data class SkillStat(
    val name: String,
    val points: Int,
    val level: Int,
    val streak: Int,
    val fundamentalsCompleted: Int = 0, // Generalize or make specific?
    val immersionHours: Double = 0.0,
    val applicationSessions: Int = 0,
    val sketchbookPages: Int = 0,
    val accountabilityPosts: Int = 0
)


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: DataRepository,
    private val appSettingsManager: AppSettingsManager // Inject AppSettingsManager
) : ViewModel() {

    private val _statistics = MutableStateFlow(AppStatistics())
    val statistics: StateFlow<AppStatistics> = _statistics.asStateFlow()

    init {
        // Combine flows from the repository to calculate statistics
        viewModelScope.launch {
            combine(
                repository.getAllSkills(),
                repository.getDissertation(),
                repository.getAllHabits(),
                repository.getAllTasks(),
                appSettingsManager.settingsFlow // Combine AppSettings flow
            ) { skills, dissertation, habits, tasks, appSettings ->
                calculateStatistics(skills, dissertation, habits, tasks, appSettings) // Pass appSettings
            }.collect { calculatedStats ->
                _statistics.value = calculatedStats
            }
        }
    }

    private fun calculateStatistics(
        skills: List<Skill>,
        dissertation: Dissertation?,
        habits: List<Habit>,
        tasks: List<TodoTask>
    ): AppStatistics {

        var totalPoints = 0
        val skillStats = mutableMapOf<String, SkillStat>()

        // Calculate skill stats
        skills.forEach { skill ->
            totalPoints += skill.points
            skillStats[skill.name] = SkillStat(
                name = skill.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, // Capitalize name
                points = skill.points,
                level = skill.level,
                streak = skill.streak,
                fundamentalsCompleted = skill.fundamentalsCompleted,
                immersionHours = skill.immersionHours,
                applicationSessions = skill.applicationSessions,
                sketchbookPages = skill.sketchbookPages,
                accountabilityPosts = skill.accountabilityPosts
            )
        }

        // Add dissertation stats (if applicable)
        dissertation?.let {
             totalPoints += it.points
             // Could add dissertation specific stats here if needed
             // For now, just include points in total
        }


        // Calculate habit completion rate (e.g., for today)
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val relevantHabitsToday = habits.filter { habit ->
             habit.active && when (habit.frequency) {
                "daily" -> true
                "weekly" -> habit.specificDays?.contains(LocalDate.now().dayOfWeek.value % 7) ?: false
                "interval" -> true // Simplification: consider all active interval habits relevant today
                else -> false
            }
        }
        val completedHabitsToday = relevantHabitsToday.count { it.completedDates.contains(todayStr) }
        val habitCompletionRate = if (relevantHabitsToday.isNotEmpty()) {
            (completedHabitsToday.toFloat() / relevantHabitsToday.size) * 100
        } else {
            0f
        }

        // Calculate task stats
        val tasksCompleted = tasks.count { it.completed }
        val tasksPending = tasks.count { !it.completed }


        return AppStatistics(
            totalPoints = totalPoints,
            skillStats = skillStats,
            habitCompletionRate = habitCompletionRate,
            tasksCompleted = tasksCompleted,
            tasksPending = tasksPending,
            generalPoints = appSettings.totalPoints // Get points from appSettings
        )
    }
}