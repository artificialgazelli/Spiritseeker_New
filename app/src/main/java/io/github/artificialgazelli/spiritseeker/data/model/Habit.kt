package com.example.spiritseeker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.spiritseeker.data.local.Converters

// --- Habit Entity (Covers Daily and Custom) ---

@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class Habit(
    @PrimaryKey val name: String,
    val icon: String,
    val active: Boolean = true,
    val isCustom: Boolean, // Differentiates between daily and custom
    val category: String? = null, // For custom habits
    val frequency: String, // "daily", "weekly", "interval"
    val interval: Int? = null, // For interval frequency
    val specificDays: List<Int>? = null, // For weekly frequency (e.g., [0, 1, 6] for Sun, Mon, Sat)
    val streak: Int = 0,
    val completedDates: List<String> = emptyList() // List of "YYYY-MM-DD"
)

// --- Habit Category Entity ---

@Entity(tableName = "habit_categories")
data class HabitCategory(
    @PrimaryKey val name: String,
    val color: String // Store color hex code e.g., "#FF5722"
)

// --- Check-In Entity ---

@Entity(tableName = "check_ins")
@TypeConverters(Converters::class)
data class CheckIn(
    @PrimaryKey val name: String, // e.g., "Doctor Appointments"
    val icon: String,
    // 'dates' and 'notes' from JSON seem less structured for direct DB storage.
    // 'subcategories' provide better structure for tracking specific appointments.
    val subcategories: List<CheckInSubcategory> = emptyList()
)

// --- CheckIn Subcategory (Stored as TypeConverted List in CheckIn) ---

data class CheckInSubcategory(
    val name: String, // e.g., "Dermatologist"
    val lastDate: String?, // "YYYY-MM-DD"
    val intervalMonths: Int?,
    val nextDate: String?, // "YYYY-MM-DD" - Can be calculated or stored
    val notes: String? = null // Add notes field
)

// --- App Settings Data Class (Stored via DataStore) ---
data class AppSettings(
    // Pomodoro
    val duration: Int = 25,
    val shortBreak: Int = 5,
    val longBreak: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartPomodoros: Boolean = false,
    // Health Check
    val healthStatus: Boolean = true, // Default to true
    val lastHealthCheck: String? = null, // "YYYY-MM-DD"
    // General Points
    val totalPoints: Int = 0
)