package com.zenodotus.ringer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.zenodotus.ringer.database.EnumTrophy
import com.zenodotus.ringer.database.TrophyWithTaskName
import com.zenodotus.ringer.viewmodels.MedalViewModel
import com.zenodotus.ringer.viewmodels.TaskViewModel
import com.zenodotus.ringer.viewmodels.TrophyViewModel

@Composable
public fun TrophiesScreen(
    navController: NavController,
    viewModel: TrophyViewModel,
    medalViewModel: MedalViewModel,
    taskViewModel: TaskViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(viewModel.getNextTrophies()) { (trophy, enumTrophy, tasksRemaining) ->
            TrophyItem(
                trophy = trophy,
                enumTrophy = enumTrophy,
                tasksRemaining = tasksRemaining
            )
        }
    }
}

@Composable
fun TrophyItem(
    trophy: TrophyWithTaskName,
    enumTrophy: EnumTrophy,      // van 0f tot 1f, berekend in ViewModel
    tasksRemaining: Int   // berekend in ViewModel
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        val badge =
            rememberAsyncImagePainter("file:///android_asset/badges/${enumTrophy.assetPath}")


        Image(
            painter = badge,
            contentDescription = enumTrophy.title,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "${enumTrophy.title}${trophy.task?.let { " voor $it" } ?: ""}",
            style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { (10 - tasksRemaining.toFloat()) / 10 },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$tasksRemaining taken te gaan",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
