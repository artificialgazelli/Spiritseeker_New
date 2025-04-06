package com.example.spiritseeker.data.local

import androidx.room.*
import com.example.spiritseeker.data.model.TodoGroup
import com.example.spiritseeker.data.model.TodoTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // --- TodoTask Methods ---
    @Query("SELECT * FROM todo_tasks ORDER BY dueDate, priority DESC, name") // Example sorting
    fun getAllTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE id = :taskId")
    fun getTaskById(taskId: String): Flow<TodoTask?>

    @Query("SELECT * FROM todo_tasks WHERE `group` = :groupName ORDER BY dueDate, priority DESC, name")
    fun getTasksByGroup(groupName: String): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE completed = 0 ORDER BY dueDate, priority DESC, name")
    fun getIncompleteTasks(): Flow<List<TodoTask>>

    @Query("SELECT * FROM todo_tasks WHERE completed = 1 ORDER BY completionDate DESC, name")
    fun getCompletedTasks(): Flow<List<TodoTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodoTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TodoTask>)

    @Update
    suspend fun updateTask(task: TodoTask)

    @Query("UPDATE todo_tasks SET completed = :completed, completionDate = :completionDate WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: String, completed: Boolean, completionDate: String?)

    @Query("DELETE FROM todo_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM todo_tasks WHERE `group` = :groupName")
    suspend fun deleteTasksByGroup(groupName: String)

    @Query("DELETE FROM todo_tasks")
    suspend fun deleteAllTasks()

    // --- TodoGroup Methods ---
    @Query("SELECT * FROM todo_groups ORDER BY name")
    fun getAllGroups(): Flow<List<TodoGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TodoGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGroups(groups: List<TodoGroup>)

    @Delete
    suspend fun deleteGroup(group: TodoGroup) // Usually delete by name, ensure tasks are handled (e.g., reassigned or deleted)

    @Query("DELETE FROM todo_groups")
    suspend fun deleteAllGroups()
}