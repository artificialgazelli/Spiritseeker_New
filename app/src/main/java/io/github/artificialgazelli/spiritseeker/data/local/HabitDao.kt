package com.example.spiritseeker.data.local

import androidx.room.*
import com.example.spiritseeker.data.model.Habit
import com.example.spiritseeker.data.model.HabitCategory
import com.example.spiritseeker.data.model.CheckIn
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // --- Habit Methods ---
    @Query("SELECT * FROM habits ORDER BY isCustom, name")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE name = :name")
    fun getHabitByName(name: String): Flow<Habit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE name = :name")
    suspend fun deleteHabitByName(name: String)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // --- HabitCategory Methods ---
    @Query("SELECT * FROM habit_categories ORDER BY name")
    fun getAllHabitCategories(): Flow<List<HabitCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCategory(category: HabitCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHabitCategories(categories: List<HabitCategory>)

    @Delete
    suspend fun deleteHabitCategory(category: HabitCategory) // Usually delete by name

    @Query("DELETE FROM habit_categories")
    suspend fun deleteAllHabitCategories()


    // --- CheckIn Methods ---
    @Query("SELECT * FROM check_ins ORDER BY name")
    fun getAllCheckIns(): Flow<List<CheckIn>>

    @Query("SELECT * FROM check_ins WHERE name = :name")
    fun getCheckInByName(name: String): Flow<CheckIn?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCheckIns(checkIns: List<CheckIn>)

    @Update
    suspend fun updateCheckIn(checkIn: CheckIn)

    @Query("DELETE FROM check_ins WHERE name = :name")
    suspend fun deleteCheckInByName(name: String)

    @Query("DELETE FROM check_ins")
    suspend fun deleteAllCheckIns()
}