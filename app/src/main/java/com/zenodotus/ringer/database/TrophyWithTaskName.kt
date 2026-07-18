package com.zenodotus.ringer.database

import java.time.LocalDate

data class TrophyWithTaskName(
    val userName: String,
    val task: String?,
    val taskId: Int?,
    val trophyType: String,
    val trophyName: String,
    val trophyValue: Int,
    val dateEarned: LocalDate = LocalDate.now()
)
