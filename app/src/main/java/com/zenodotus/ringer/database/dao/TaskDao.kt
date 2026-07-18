package com.zenodotus.ringer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenodotus.ringer.database.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("select * from task where assignedToUserName = :username and not (frequency = 'Eenmalig' AND (completedCount > 0 OR failedCount > 0))")
    fun getTasks(username: String): Flow<List<Task>>

    @Query("select * from task where taskId = :taskId")
    suspend fun getTaskById(taskId: Int): Task

    @Query("select * from task where taskId = :taskId")
    fun getTaskByIdFlow(taskId: Int): Flow<Task?>
}

