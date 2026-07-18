package com.zenodotus.ringer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zenodotus.ringer.database.Medal
import java.time.LocalDate

@Dao
interface MedalDao {

    @Insert
    suspend fun insert(medal: Medal)

    @Query("SELECT COUNT(*) FROM Medal WHERE userName = :user AND dateEarned = :today")
    suspend fun countTodaysMedals(user: String, today: LocalDate): Int

    @Query("SELECT * FROM Medal WHERE userName = :user AND dateEarned = :today")
    suspend fun getTodaysMedals(user: String, today: LocalDate): List<Medal>

    @Query("SELECT COUNT(*) FROM Medal WHERE userName = :user AND taskId = :taskId")
    suspend fun countMedalsPerTask(user: String, taskId: Int): Int

    @Query("SELECT * FROM Medal WHERE userName = :user AND taskId = :taskId order by dateEarned desc")
    suspend fun getMedalsPerTask(user: String, taskId: Int): List<Medal>
}