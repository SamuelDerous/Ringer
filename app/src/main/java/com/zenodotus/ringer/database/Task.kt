package com.zenodotus.ringer.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalTime

@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userName"],
            childColumns = ["assignedToUserName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assignedToUserName")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val taskId: Int = 0,
    val task: String,
    val picto: String?,
    val day: Int,
    val time: LocalTime,
    val frequency: String,
    val frequencyValue: Int,
    val archived: Boolean = false,
    val assignedToUserName: String,
    var lastCompleted: Long? = null,
    var completedCount: Int = 0,
    var failedCount: Int = 0

) : Serializable
