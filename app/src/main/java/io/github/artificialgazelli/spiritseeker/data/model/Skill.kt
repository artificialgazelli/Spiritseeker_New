package com.example.spiritseeker.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.spiritseeker.data.local.Converters

// --- Base Skill Entity ---

@Entity(tableName = "skills")
@TypeConverters(Converters::class)
data class Skill(
    @PrimaryKey val name: String, // "art", "korean", "french"
    val points: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastPractice: String? = null, // Store as ISO date string e.g., "YYYY-MM-DD"
    val fundamentalsCompleted: Int = 0,
    val immersionHours: Double = 0.0, // For languages
    val applicationSessions: Int = 0, // For languages
    val sketchbookPages: Int = 0, // For art
    val accountabilityPosts: Int = 0, // For art
    val rewardsUnlocked: List<String> = emptyList(),
    @Embedded val exercises: SkillExercises? = null,
    val completedExercises: List<CompletedExercise> = emptyList(), // Specific to Art
    val completedLessons: List<CompletedLesson> = emptyList(), // Specific to Languages
    val drawingLog: List<DrawingLogEntry> = emptyList(), // Specific to Art
    val immersionLog: List<ImmersionLogEntry> = emptyList(), // Specific to Languages
    val applicationLog: List<ApplicationLogEntry> = emptyList() // Specific to Languages
)

// --- Embedded Exercise Lists ---

data class SkillExercises(
    val fundamentals: List<String> = emptyList(),
    val sketchbook: List<String> = emptyList(), // Art specific
    val accountability: List<String> = emptyList(), // Art specific
    val immersion: List<String> = emptyList(), // Language specific
    val application: List<String> = emptyList() // Language specific
)

// --- Log/Completion Entries (Can be stored as TypeConverted Lists in Skill) ---

data class CompletedExercise( // For Art
    val exercise: String,
    val type: String, // e.g., "fundamentals"
    val timestamp: String, // Store as ISO datetime string e.g., "YYYY-MM-DD HH:MM"
    val points: Int
)

data class CompletedLesson( // For Languages
    val lesson: String,
    val type: String, // e.g., "fundamentals"
    val timestamp: String, // Store as ISO datetime string e.g., "YYYY-MM-DD HH:MM"
    val points: Int
)

data class DrawingLogEntry( // For Art
    val type: String, // e.g., "Still life"
    val notes: String?,
    val timestamp: String, // Store as ISO datetime string e.g., "YYYY-MM-DD HH:MM"
    val points: Int
)

data class ImmersionLogEntry( // For Languages
    val type: String, // e.g., "Watch K-drama (30 min)"
    val title: String?,
    val duration: String?, // e.g., "30 minutes"
    val hours: Double,
    val timestamp: String, // Store as ISO datetime string e.g., "YYYY-MM-DD HH:MM"
    val points: Int
)

data class ApplicationLogEntry( // For Languages
    val type: String, // e.g., "Order at restaurant in French"
    val notes: String?,
    val timestamp: String, // Store as ISO datetime string e.g., "YYYY-MM-DD HH:MM"
    val points: Int
)