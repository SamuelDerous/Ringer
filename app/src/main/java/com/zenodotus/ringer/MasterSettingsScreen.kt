package com.zenodotus.ringer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import com.zenodotus.ringer.viewmodels.UserSettingsViewModel


@Composable
fun MasterSettingsScreen(
    navController: NavController,
    viewModel: UserSettingsViewModel,
    colorViewModel: ColorSettingsViewModel
) {
    val userSettings by viewModel.userSettings.collectAsState()

    userSettings?.let { settings ->

        Column(modifier = Modifier.padding(40.dp)) {

            Text("Instellingen")

            Spacer(Modifier.height(24.dp))

            // MASTER SWITCH
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Master instellingen aan")

                Switch(
                    checked = settings.settings,
                    onCheckedChange = { newValue ->
                        viewModel.updateSettings(
                            settings.copy(
                                settings = newValue,
                                intrusionAlert = if (!newValue) false else settings.intrusionAlert,
                                persist = if (!newValue) false else settings.persist,
                                timeBetween = if (!newValue) 0 else settings.timeBetween,
                                amountBetween = if (!newValue) 0 else settings.amountBetween,
                                alarmNonStop = if (!newValue) false else settings.alarmNonStop
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            // INTRUSION ALERT
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarm altijd aan")

                Switch(
                    checked = settings.intrusionAlert,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(intrusionAlert = it)
                        )
                    },
                    enabled = settings.settings
                )
            }

            Spacer(Modifier.height(24.dp))

            // INTRUSION ALERT
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarm herhalen tot stop")

                Switch(
                    checked = settings.alarmNonStop,
                    onCheckedChange = {
                        viewModel.updateSettings(
                            settings.copy(alarmNonStop = it)
                        )
                        if (it) colorViewModel.setNonStop(true)
                    },
                    enabled = settings.settings
                )
            }

            Spacer(Modifier.height(24.dp))

            // PERSIST
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kan alarmen niet stoppen")

                Switch(
                    checked = settings.persist,
                    onCheckedChange = { newValue ->
                        viewModel.updateSettings(
                            settings.copy(
                                persist = newValue,
                                amountBetween = if (newValue) 0 else settings.amountBetween
                            )
                        )
                    },
                    enabled = settings.settings
                )
            }

            Spacer(Modifier.height(24.dp))

            // TIME BETWEEN
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Snooze tijd")

                SnoozeNumberPicker(
                    value = settings.timeBetween,
                    onValueChange = {
                        viewModel.updateSettings(
                            settings.copy(timeBetween = it)
                        )
                    },
                    min = 1,
                    max = 60,
                    step = 1,
                    cap = "m",
                    enabled = settings.settings
                )
            }

            Spacer(Modifier.height(24.dp))

            // AMOUNT BETWEEN
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aantal snoozen")

                SnoozeNumberPicker(
                    value = settings.amountBetween,
                    onValueChange = {
                        viewModel.updateSettings(
                            settings.copy(amountBetween = it)
                        )
                    },
                    min = 1,
                    max = 60,
                    step = 1,
                    cap = "",
                    enabled = settings.settings && !settings.persist
                )
            }
        }
    }
}