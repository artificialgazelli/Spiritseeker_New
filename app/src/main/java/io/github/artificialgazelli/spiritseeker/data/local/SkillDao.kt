package com.example.spiritseeker.data.local

import androidx.room.*
import com.example.spiritseeker.data.model.Skill
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {

    @Query("SELECT * FROM skills")
    fun getAllSkills(): Flow<List<Skill>>

    @Query("SELECT * FROM skills WHERE name = :skillName")
    fun getSkillByName(skillName: String): Flow<Skill?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSkills(skills: List<Skill>)

    @Update
    suspend fun updateSkill(skill: Skill)

    @Query("UPDATE skills SET points = :points, level = :level, streak = :streak, lastPractice = :lastPractice WHERE name = :skillName")
    suspend fun updateSkillProgress(skillName: String, points: Int, level: Int, streak: Int, lastPractice: String?)

    // Add more specific update queries as needed, e.g., for logs, completed exercises etc.
    // Example:
    // @Query("UPDATE skills SET completedExercises = :completedExercises WHERE name = :skillName")
    // suspend fun updateCompletedExercises(skillName: String, completedExercises: List<CompletedExercise>)

    @Query("DELETE FROM skills WHERE name = :skillName")
    suspend fun deleteSkillByName(skillName: String)

    @Query("DELETE FROM skills")
    suspend fun deleteAllSkills()
}