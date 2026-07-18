package com.zenodotus.ringer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenodotus.ringer.database.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): User?

    @Query("select count(username) FROM users WHERE username = :username")
    suspend fun getUsernames(username: String): Int

    @Query("UPDATE users SET masterPass = :masterPass WHERE username = :username")
    suspend fun setMasterPassword(username: String, masterPass: String)

    @Query("UPDATE users SET avatar = :avatar WHERE username = :username")
    suspend fun changeAvatar(username: String, avatar: String)
}