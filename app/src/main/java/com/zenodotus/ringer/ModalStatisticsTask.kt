package com.zenodotus.ringer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.rememberAsyncImagePainter
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.viewmodels.MedalViewModel
import com.zenodotus.ringer.viewmodels.TaskViewModel
import com.zenodotus.ringer.viewmodels.TrophyViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalStatisticsTask(
    taskId: Int = 0,
    viewModel: TaskViewModel,
    medalViewModel: MedalViewModel,
    trophyViewModel: TrophyViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val task by viewModel.findTaskByIdFlow(taskId).collectAsState(null)
    val userPrefs = UserPreferences(LocalContext.current)
    val medalsPerTask = medalViewModel.medalsOfTask.collectAsState()
    val streaks = medalViewModel.streak.collectAsState()
    val lastCompletedMillis = task?.lastCompleted
    val scope = rememberCoroutineScope()
    LaunchedEffect(taskId) {
        val userName = userPrefs.getUsername()
        if (userName != null) {
            medalViewModel.medalsPerTask(userName, taskId)
            medalViewModel.getStreaks(userName, taskId)
        }

    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconSlot {
                        Text("🥇", fontSize = 24.sp) // 👈 kleiner maken!
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${medalsPerTask.value}× voor deze taak", fontSize = 12.sp)
                }
                val trophy = trophyViewModel.getEnumTrophy(taskId)
                val badge =
                    rememberAsyncImagePainter("file:///android_asset/badges/${trophy?.assetPath}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconSlot {
                        Image(
                            painter = badge,
                            contentDescription = trophy?.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (trophy == null || trophy.typeName == "INIT_STREAK")
                            "Nog geen trofee voor deze taak"
                        else trophy.title,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconSlot {
                        Text("🔥", fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Streak: ${streaks.value}", fontSize = 12.sp)
                }

                if (lastCompletedMillis != null) {
                    val instant = Instant.ofEpochMilli(lastCompletedMillis!!)
                    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                    val displayDate = localDate.format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM yyyy",
                            Locale.getDefault()
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconSlot {
                            DateIcon(displayDate)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Laatst voltooid: $displayDate", fontSize = 12.sp)
                    }
                }

            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text("Sluiten")
                }

            }
        }
    }
}
