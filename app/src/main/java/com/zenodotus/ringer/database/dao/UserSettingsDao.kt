package com.zenodotus.ringer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenodotus.ringer.database.UserSetting

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSettings: UserSetting): Long

    @Update
    suspend fun updateSettings(userSettings: UserSetting)

    @Delete
    suspend fun deleteSettings(userSettings: UserSetting)

    @Query("select * from user_setting where username = :username")
    suspend fun getUserSettings(username: String): UserSetting?

    @Query("UPDATE user_setting SET settings = :value WHERE userName = :userName")
    suspend fun updateSettings(userName: String, value: Boolean)

    @Query("UPDATE user_setting SET intrusionAlert = :value WHERE userName = :userName")
    suspend fun updateIntrusionAlert(userName: String, value: Boolean)

    @Query("UPDATE user_setting SET persist = :value WHERE userName = :userName")
    suspend fun updatePersist(userName: String, value: Boolean)

    @Query("UPDATE user_setting SET timeBetween = :value WHERE userName = :userName")
    suspend fun updateTimeBetween(userName: String, value: Int)

    @Query("UPDATE user_setting SET amountBetween = :value WHERE userName = :userName")
    suspend fun updateAmountBetween(userName: String, value: Int)
}

