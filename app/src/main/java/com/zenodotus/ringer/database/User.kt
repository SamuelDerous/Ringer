package com.zenodotus.ringer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userName: String,
    val avatar: String,
    val passwordHash: String,
    val email: String,
    var masterPass: String? = null
)
