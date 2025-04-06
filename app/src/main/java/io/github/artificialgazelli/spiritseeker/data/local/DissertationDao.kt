package com.example.spiritseeker.data.local

import androidx.room.*
import com.example.spiritseeker.data.model.Dissertation
import kotlinx.coroutines.flow.Flow

@Dao
interface DissertationDao {

    // Since there's likely only one dissertation entry, get it directly
    @Query("SELECT * FROM dissertation WHERE id = :id LIMIT 1")
    fun getDissertation(id: String = "dissertation_main"): Flow<Dissertation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDissertation(dissertation: Dissertation)

    @Update
    suspend fun updateDissertation(dissertation: Dissertation)

    // Example of a more specific update if needed
    @Query("UPDATE dissertation SET points = :points, level = :level, streak = :streak, lastPractice = :lastPractice WHERE id = :id")
    suspend fun updateDissertationProgress(id: String = "dissertation_main", points: Int, level: Int, streak: Int, lastPractice: String?)

    // Add specific updates for tasks if necessary, e.g., updating hours_worked for a specific task
    // This might be complex with nested lists and might be better handled by fetching the
    // Dissertation object, modifying it in the ViewModel, and then calling updateDissertation.

    @Query("DELETE FROM dissertation WHERE id = :id")
    suspend fun deleteDissertation(id: String = "dissertation_main")
}