package com.zenodotus.ringer.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_setting",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userName"],
            childColumns = ["userName"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    )
data class UserSetting(
    @PrimaryKey val userName: String,
    val settings: Boolean = true,
    val intrusionAlert: Boolean = true,
    val alarmNonStop: Boolean = true,
    val persist: Boolean = true,
    val timeBetween: Int = 0,
    val amountBetween: Int = 0
)
