package com.example.spiritseeker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.spiritseeker.data.model.* // Import all models

@Database(
    entities = [
        Skill::class, Dissertation::class, // Existing
        Habit::class, HabitCategory::class, CheckIn::class, // Habits/CheckIns
        TodoTask::class, TodoGroup::class // ToDo
    ],
    version = 2, // Incremented version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun skillDao(): SkillDao
    abstract fun dissertationDao(): DissertationDao
    abstract fun habitDao(): HabitDao
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spirit_seeker_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this basic implementation.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}