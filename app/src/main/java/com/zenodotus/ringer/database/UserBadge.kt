package com.zenodotus.ringer.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userName"],
            childColumns = ["userName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserBadge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val badgeName: Int,
    val dateEarned: String,
    val test: String,
    val test2: String,
    val test3: String,
    val test4: String
)
