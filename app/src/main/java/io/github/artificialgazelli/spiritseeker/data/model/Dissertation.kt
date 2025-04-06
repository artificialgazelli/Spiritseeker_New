package com.example.spiritseeker.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.spiritseeker.data.local.Converters

@Entity(tableName = "dissertation")
@TypeConverters(Converters::class)
data class Dissertation(
    @PrimaryKey val id: String = "dissertation_main", // Single entry for dissertation
    val points: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastPractice: String? = null, // Store as ISO date string e.g., "YYYY-MM-DD"
    @Embedded val tasks: DissertationTasks? = null
)

data class DissertationTasks(
    val preparation: List<DissertationTask> = emptyList(),
    val empirical: List<DissertationTask> = emptyList(),
    val integration: List<DissertationTask> = emptyList(),
    val finalization: List<DissertationTask> = emptyList()
)

// Note: DissertationTask can be stored as a TypeConverted List within DissertationTasks
data class DissertationTask(
    val name: String,
    val startDate: String, // Store as "DD.MM.YYYY" or convert to ISO on load/save
    val endDate: String,   // Store as "DD.MM.YYYY" or convert to ISO on load/save
    val totalHours: Int,
    val hoursWorked: Double = 0.0 // Use Double for potentially fractional hours logged
)