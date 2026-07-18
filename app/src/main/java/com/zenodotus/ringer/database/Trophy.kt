package com.zenodotus.ringer.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userName"],
            childColumns = ["userName"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = Task::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userName"), Index("taskId")]
)
data class Trophy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val taskId: Int?,
    val trophyType: String,
    val trophyName: String,
    val trophyValue: Int,
    val dateEarned: LocalDate = LocalDate.now()
)