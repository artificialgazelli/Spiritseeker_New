package com.example.spiritseeker.data.local

import androidx.room.TypeConverter
import com.example.spiritseeker.data.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- List<String> Converters ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- SkillExercises Converter ---
    @TypeConverter
    fun fromSkillExercises(value: SkillExercises?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toSkillExercises(value: String?): SkillExercises? {
        return value?.let { gson.fromJson(it, SkillExercises::class.java) }
    }

    // --- List<CompletedExercise> Converter ---
    @TypeConverter
    fun fromCompletedExerciseList(value: List<CompletedExercise>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCompletedExerciseList(value: String?): List<CompletedExercise>? {
        return value?.let {
            val listType = object : TypeToken<List<CompletedExercise>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- List<CompletedLesson> Converter ---
    @TypeConverter
    fun fromCompletedLessonList(value: List<CompletedLesson>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCompletedLessonList(value: String?): List<CompletedLesson>? {
        return value?.let {
            val listType = object : TypeToken<List<CompletedLesson>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- List<DrawingLogEntry> Converter ---
    @TypeConverter
    fun fromDrawingLogEntryList(value: List<DrawingLogEntry>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDrawingLogEntryList(value: String?): List<DrawingLogEntry>? {
        return value?.let {
            val listType = object : TypeToken<List<DrawingLogEntry>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- List<ImmersionLogEntry> Converter ---
    @TypeConverter
    fun fromImmersionLogEntryList(value: List<ImmersionLogEntry>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toImmersionLogEntryList(value: String?): List<ImmersionLogEntry>? {
        return value?.let {
            val listType = object : TypeToken<List<ImmersionLogEntry>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- List<ApplicationLogEntry> Converter ---
    @TypeConverter
    fun fromApplicationLogEntryList(value: List<ApplicationLogEntry>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toApplicationLogEntryList(value: String?): List<ApplicationLogEntry>? {
        return value?.let {
            val listType = object : TypeToken<List<ApplicationLogEntry>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }

    // --- DissertationTasks Converter ---
     @TypeConverter
    fun fromDissertationTasks(value: DissertationTasks?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDissertationTasks(value: String?): DissertationTasks? {
        return value?.let { gson.fromJson(it, DissertationTasks::class.java) }
    }

    // --- List<DissertationTask> Converter (Used within DissertationTasks) ---
    // Note: Room handles nested TypeConverters, but explicitly defining it can be clearer
    // If DissertationTask itself had complex types, you'd need converters for those too.
    // Since DissertationTask only has primitives/Strings, Gson handles it directly within
    // the DissertationTasks converter. If you stored List<DissertationTask> directly
    // in the Dissertation entity, you would need this converter:
    /*
    @TypeConverter
    fun fromDissertationTaskList(value: List<DissertationTask>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDissertationTaskList(value: String?): List<DissertationTask>? {
        return value?.let {
            val listType = object : TypeToken<List<DissertationTask>>() {}.type
            gson.fromJson(it, listType)
        } ?: emptyList()
    }
    */
}