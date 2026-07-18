package com.zenodotus.ringer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zenodotus.ringer.database.Trophy
import com.zenodotus.ringer.database.TrophyWithTaskName
import kotlinx.coroutines.flow.Flow

@Dao
interface TrophyDao {

    @Insert
    suspend fun insert(trophy: Trophy)

    @Query(
        "update trophy set trophyValue = trophyValue + 1, " +
                "trophyName = :name where userName = :userName and taskId = :taskId and trophyType = :trophyType"
    )
    suspend fun updateTrophy(userName: String, taskId: Int, name: String, trophyType: String)

    @Query(
        "update trophy set trophyValue = trophyValue + 1, " +
                "trophyName = :name where userName = :userName and trophyType = :trophyType"
    )
    suspend fun updateTrophy(userName: String, name: String, trophyType: String)

    @Query("select * from trophy where userName = :userName and taskId = :taskId and trophyType = :type")
    suspend fun getTrophy(userName: String, taskId: Int, type: String): Trophy?

    @Query(
        "select t.username, ta.task, ta.taskId, t.trophyValue, t.trophyType, t.trophyName, t.dateEarned " +
                "from trophy t left join task ta on t.taskId = ta.taskId where (t.trophyType = 'cumulative' or ta.archived = 0) and " +
                "userName = :userName"
    )
    fun getTrophiesOfUser(userName: String): Flow<List<TrophyWithTaskName>>

    @Query("delete from trophy where taskId = :taskId")
    suspend fun deleteTrophy(taskId: Int)
}


