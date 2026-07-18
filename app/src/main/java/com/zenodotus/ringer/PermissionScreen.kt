package com.zenodotus.ringer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zenodotus.ringer.viewmodels.PermissionsViewModel

@Composable
fun PermissionsScreen(
    navController: NavController,
    viewModel: PermissionsViewModel,
    modifier: Modifier = Modifier,
    onRequestNotification: () -> Unit,
    onRequestBattery: () -> Unit
) {
    val notificationsGranted by viewModel.notificationsGranted.collectAsState()
    val batteryExempt by viewModel.batteryExempt.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Deze app heeft permissies nodig om correct te functioneren.")

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequestNotification() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Notificaties toestaan",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = notificationsGranted,
                    onCheckedChange = null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequestBattery() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "App actief houden in achtergrond",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = batteryExempt,
                    onCheckedChange = null
                )
            }
        }
    }
}
