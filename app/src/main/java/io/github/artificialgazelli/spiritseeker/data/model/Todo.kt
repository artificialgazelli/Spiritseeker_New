package com.example.spiritseeker.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.spiritseeker.data.local.Converters
import java.util.UUID

// --- ToDo Task Entity ---

@Entity(tableName = "todo_tasks")
@TypeConverters(Converters::class)
data class TodoTask(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val group: String, // Category/Group name
    val priority: String, // "High", "Medium", "Low"
    val dueDate: String?, // "YYYY-MM-DD"
    val completed: Boolean = false,
    val completionDate: String? = null, // "YYYY-MM-DD"
    val notes: String? = null,
    @Embedded val recurrence: Recurrence? = null
)

// --- Recurrence Info (Embedded in TodoTask) ---

data class Recurrence(
    val type: String, // "daily", "weekly", "monthly", "yearly"
    val interval: Int,
    val days: List<Int>? = null, // For weekly recurrence (0=Sun, 1=Mon, ...)
    val endDate: String? = null // "YYYY-MM-DD"
)

// --- ToDo Group Entity ---

@Entity(tableName = "todo_groups")
data class TodoGroup(
    @PrimaryKey val name: String,
    val color: String // Store color hex code e.g., "#FFC107"
)