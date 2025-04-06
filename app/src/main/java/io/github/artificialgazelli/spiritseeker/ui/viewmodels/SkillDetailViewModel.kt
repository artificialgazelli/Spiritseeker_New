package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spiritseeker.AppDestinations
import com.example.spiritseeker.data.AppSettingsManager // Import AppSettingsManager
import com.example.spiritseeker.data.model.*
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate // Import LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class SkillDetailViewModel @Inject constructor(
    private val repository: DataRepository,
    private val appSettingsManager: AppSettingsManager, // Inject AppSettingsManager
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Observe health status
    // Renamed healthSettings to appSettings
    private val appSettings = appSettingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) // Keep null default or use AppSettings()?

    val skillId: String = savedStateHandle.get<String>(AppDestinations.SKILL_ID_ARG) ?: "unknown"

    // Flow for the specific skill being viewed
    val skill: StateFlow<Skill?> = repository.getSkillByName(skillId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Gamification Constants ---
    // Example thresholds, adjust as needed
    private val levelThresholds = listOf(0, 100, 250, 500, 1000, 2000, 5000) // Points needed for level index + 1

    // Predefined rewards (simplified, ideally fetched from repository/shared source)
    private val smallRewards = listOf("New art supplies (pencils, pens)", "Korean snacks package", "French pastry treat", "Download a new playlist", "Movie night") // Example subset
    private val mediumRewards = listOf("Art instruction book", "Korean webtoon collection", "French film collection", "Nice sketchbook or journal", "Language learning app subscription (1 month)")
    private val largeRewards = listOf("Premium art course", "TOPIK prep materials full set", "Trip to a French cafe or restaurant", "Art software or digital tools", "Language tutoring session")

    // --- Actions to Log Progress ---

    // Example: Log completing a fundamental exercise (Art/Language)
    fun logFundamentalCompleted(exerciseName: String) {
        viewModelScope.launch {
            val currentSkill = skill.value ?: return@launch
            var pointsEarned = 2 // Example points
            if (appSettings.value?.healthStatus == false) pointsEarned = 0 // Check appSettings

            val updatedSkill = currentSkill.copy(
                points = currentSkill.points + pointsEarned,
                fundamentalsCompleted = currentSkill.fundamentalsCompleted + 1,
                // Add to completed list (handle both exercise/lesson types)
                completedExercises = if (skillId == "art") {
                    currentSkill.completedExercises + CompletedExercise(
                        exercise = exerciseName,
                        type = "fundamentals",
                        timestamp = getCurrentTimestamp(),
                        points = pointsEarned
                    )
                } else { currentSkill.completedExercises },
                completedLessons = if (skillId != "art") {
                     currentSkill.completedLessons + CompletedLesson(
                        lesson = exerciseName,
                        type = "fundamentals",
                        timestamp = getCurrentTimestamp(),
                        points = pointsEarned
                    )
                } else { currentSkill.completedLessons },
                lastPractice = getCurrentDate(), // Update last practice date
                streak = calculateNewStreak(currentSkill.lastPractice, currentSkill.streak) // Calculate new streak
            )
            // Calculate new level based on updated points
            val newLevel = calculateLevel(updatedSkill.points)
            val unlockedReward = checkRewardUnlock(currentSkill.level, newLevel, currentSkill.rewardsUnlocked)

            val finalSkill = updatedSkill.copy(
                 level = newLevel,
                 rewardsUnlocked = if (unlockedReward != null) currentSkill.rewardsUnlocked + unlockedReward else currentSkill.rewardsUnlocked
            )
            repository.updateSkill(finalSkill)
            if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
        }
    }

    // Example: Log immersion time (Language)
    fun logImmersion(type: String, hours: Double, title: String? = null) {
         viewModelScope.launch {
            val currentSkill = skill.value ?: return@launch
            if (skillId == "art" || skillId == "diss") return@launch // Only for languages

            var pointsEarned = (hours * 10).toInt() // Example: 1 point per 6 mins
            if (appSettings.value?.healthStatus == false) pointsEarned = 0 // Check appSettings

            val updatedSkill = currentSkill.copy(
                points = currentSkill.points + pointsEarned,
                immersionHours = currentSkill.immersionHours + hours,
                immersionLog = currentSkill.immersionLog + ImmersionLogEntry(
                    type = type,
                    title = title,
                    duration = "${(hours * 60).toInt()} minutes", // Approximate duration string
                    hours = hours,
                    timestamp = getCurrentTimestamp(),
                    points = pointsEarned
                ),
                lastPractice = getCurrentDate(),
                streak = calculateNewStreak(currentSkill.lastPractice, currentSkill.streak)
            )
            val newLevel = calculateLevel(updatedSkill.points)
            val unlockedReward = checkRewardUnlock(currentSkill.level, newLevel, currentSkill.rewardsUnlocked)

            val finalSkill = updatedSkill.copy(
                 level = newLevel,
                 rewardsUnlocked = if (unlockedReward != null) currentSkill.rewardsUnlocked + unlockedReward else currentSkill.rewardsUnlocked
            )
            repository.updateSkill(finalSkill)
            if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
        }
    }

     // Example: Log application session (Language)
    fun logApplication(type: String, notes: String? = null) {
         viewModelScope.launch {
            val currentSkill = skill.value ?: return@launch
             if (skillId == "art" || skillId == "diss") return@launch // Only for languages

            var pointsEarned = 10 // Example points per session
            if (appSettings.value?.healthStatus == false) pointsEarned = 0 // Check appSettings

            val updatedSkill = currentSkill.copy(
                points = currentSkill.points + pointsEarned,
                applicationSessions = currentSkill.applicationSessions + 1,
                applicationLog = currentSkill.applicationLog + ApplicationLogEntry(
                    type = type,
                    notes = notes,
                    timestamp = getCurrentTimestamp(),
                    points = pointsEarned
                ),
                lastPractice = getCurrentDate(),
                streak = calculateNewStreak(currentSkill.lastPractice, currentSkill.streak)
            )
            val newLevel = calculateLevel(updatedSkill.points)
            val unlockedReward = checkRewardUnlock(currentSkill.level, newLevel, currentSkill.rewardsUnlocked)

             val finalSkill = updatedSkill.copy(
                 level = newLevel,
                 rewardsUnlocked = if (unlockedReward != null) currentSkill.rewardsUnlocked + unlockedReward else currentSkill.rewardsUnlocked
            )
            repository.updateSkill(finalSkill)
            if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
        }
    }

     // Example: Log drawing (Art)
    fun logDrawing(type: String, notes: String? = null) {
         viewModelScope.launch {
            val currentSkill = skill.value ?: return@launch
            if (skillId != "art") return@launch // Only for art

            var pointsEarned = 5 // Example points per drawing
            if (appSettings.value?.healthStatus == false) pointsEarned = 0 // Check appSettings

            val updatedSkill = currentSkill.copy(
                points = currentSkill.points + pointsEarned,
                sketchbookPages = currentSkill.sketchbookPages + 1, // Assuming 1 drawing = 1 page for simplicity
                drawingLog = currentSkill.drawingLog + DrawingLogEntry(
                    type = type,
                    notes = notes,
                    timestamp = getCurrentTimestamp(),
                    points = pointsEarned
                ),
                lastPractice = getCurrentDate(),
                streak = calculateNewStreak(currentSkill.lastPractice, currentSkill.streak)
            )
            val newLevel = calculateLevel(updatedSkill.points)
            val unlockedReward = checkRewardUnlock(currentSkill.level, newLevel, currentSkill.rewardsUnlocked)

             val finalSkill = updatedSkill.copy(
                 level = newLevel,
                 rewardsUnlocked = if (unlockedReward != null) currentSkill.rewardsUnlocked + unlockedReward else currentSkill.rewardsUnlocked
            )
            repository.updateSkill(finalSkill)
            if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
        }
    }

    // Example: Log accountability task (Art)
    fun logAccountability(type: String) {
         viewModelScope.launch {
            val currentSkill = skill.value ?: return@launch
            if (skillId != "art") return@launch // Only for art

            val pointsEarned = 15 // Example points for accountability

            val updatedSkill = currentSkill.copy(
                points = currentSkill.points + pointsEarned,
                accountabilityPosts = currentSkill.accountabilityPosts + 1,
                // Optionally add to a specific log if needed
                lastPractice = getCurrentDate(),
                streak = calculateNewStreak(currentSkill.lastPractice, currentSkill.streak)
            )
            val newLevel = calculateLevel(updatedSkill.points)
            val unlockedReward = checkRewardUnlock(currentSkill.level, newLevel, currentSkill.rewardsUnlocked)

             val finalSkill = updatedSkill.copy(
                 level = newLevel,
                 rewardsUnlocked = if (unlockedReward != null) currentSkill.rewardsUnlocked + unlockedReward else currentSkill.rewardsUnlocked
            )
            repository.updateSkill(finalSkill)
            if (pointsEarned > 0) appSettingsManager.addTotalPoints(pointsEarned) // Add points
            // println("Logged accountability: $type") // Can remove print
        }
    }

    // TODO: Add functions for logging dissertation hours, etc.
    // --- Gamification Helper Functions ---

    private fun calculateLevel(points: Int): Int {
        // Find the highest level threshold the points meet or exceed
        val levelIndex = levelThresholds.indexOfLast { points >= it }
        return levelIndex + 1 // Levels are 1-based
    }
// calculateNewStreak is now in DataRepository

// --- Streak Check Logic ---
private suspend fun checkAndResetStreakIfNeeded(currentSkill: Skill): Skill {
    val today = LocalDate.now()
    val lastPracticeDate = currentSkill.lastPractice?.let { try { LocalDate.parse(it) } catch (e: Exception) { null } }

    if (lastPracticeDate != null && lastPracticeDate.isBefore(today.minusDays(1))) {
        // Streak is broken (last practice was before yesterday)
        if (currentSkill.streak > 0) {
            val updatedSkill = currentSkill.copy(streak = 0)
            repository.updateSkill(updatedSkill) // Update DB
            return updatedSkill // Return updated skill for the flow
        }
    }
    return currentSkill // Return original skill if streak is okay or already 0
}

private fun checkRewardUnlock(oldLevel: Int, newLevel: Int, alreadyUnlocked: List&lt;String&gt;): String? {
    if (newLevel <= oldLevel) return null // No level up

    val newlyUnlockedRewards = mutableListOf&lt;String&gt;()

    // Check level thresholds for rewards
    if (newLevel >= 3 && oldLevel < 3) {
         smallRewards.shuffled().firstOrNull { !alreadyUnlocked.contains(it) }?.let { newlyUnlockedRewards.add(it) }
    }
    if (newLevel >= 5 && oldLevel < 5) {
         mediumRewards.shuffled().firstOrNull { !alreadyUnlocked.contains(it) }?.let { newlyUnlockedRewards.add(it) }
    }
     if (newLevel >= 10 && oldLevel < 10) {
         largeRewards.shuffled().firstOrNull { !alreadyUnlocked.contains(it) }?.let { newlyUnlockedRewards.add(it) }
    }

    // Return one reward if multiple were unlocked simultaneously (unlikely with current thresholds)
    return newlyUnlockedRewards.firstOrNull()

    // getCurrentDate is now in DataRepository

    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) // Keep timestamp for logs
    }
}