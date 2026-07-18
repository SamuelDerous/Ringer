package com.zenodotus.ringer

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import com.zenodotus.ringer.viewmodels.UserSettingsViewModel


@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ColorSettingsViewModel,
    userSettingsViewModel: UserSettingsViewModel
) {
    val alarmSound by viewModel.alarmSound.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val nonStop by viewModel.nonStop.collectAsState()
    val alarmColorHex by viewModel.alarmColor.collectAsState(initial = "#FF0000")
    val backgroundColorHex by viewModel.backgroundColor.collectAsState(initial = "#FFFFFF")
    val snoozetime by viewModel.snoozeTime.collectAsState()
    val addColorHex by viewModel.addColor.collectAsState(initial = "#FFFFFF")
    var colorKey by remember { mutableStateOf("") }
    var currentColor by remember { mutableStateOf(Color.Red) }
    var alarmUri by remember { mutableStateOf<Uri?>(null) }
    val userSettings by userSettingsViewModel.userSettings.collectAsState()
    val settings = userSettings

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        // GEBRUIK OpenDocument IN PLAATS VAN GetContent
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // 1. Vraag permanente leesrechten aan
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)

                // 2. Sla de URI nu pas op
                viewModel.updateAlarmSound(it.toString())
            } catch (e: Exception) {
                Log.e("App", "Kon rechten niet vastleggen: ${e.message}")
                // Sla hem alsnog op, de Receiver zal dan de fallback gebruiken
                viewModel.updateAlarmSound(it.toString())
            }
        }
    }

    val alarmColor = try {
        Color(android.graphics.Color.parseColor(alarmColorHex))
    } catch (e: Exception) {
        Color.Red // fallback als string ongeldig is
    }

    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(backgroundColorHex))
    } catch (e: Exception) {
        Color.Red // fallback als string ongeldig is
    }

    val addColor = try {
        Color(android.graphics.Color.parseColor(addColorHex))
    } catch (e: Exception) {
        Color.Red // fallback als string ongeldig is
    }

    var tempSound by remember { mutableStateOf(alarmSound ?: "") }


    var showColorPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Instellingen",
                style = MaterialTheme.typography.headlineMedium, // mooie grote titel
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Alarmkleur")

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .background(alarmColor)
                        .clickable {
                            colorKey = "alarm_color"
                            currentColor = alarmColor
                            showColorPicker = true
                        }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Achtergrondkleur taken")

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .background(backgroundColor)
                        .clickable {
                            colorKey = "background_color"
                            currentColor = backgroundColor
                            showColorPicker = true
                        }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Kleur toevoegenknop")

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .background(addColor)
                        .clickable {
                            colorKey = "add_color"
                            currentColor = addColor
                            showColorPicker = true
                        }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Snooze tijd")

                SnoozeNumberPicker(
                    value = if (settings != null && (settings.settings && settings.timeBetween > 0)) settings.timeBetween else snoozetime,
                    onValueChange = { viewModel.setSnoozeTime(it) },
                    min = 1,
                    max = 60,
                    step = 1,
                    cap = "m",
                    enabled = if (settings != null && settings.settings && settings.timeBetween > 0) false else true,

                    )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Alarmgeluid")

                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSound(it) },
                    enabled = if (settings != null && (settings.settings && settings.intrusionAlert)) false else true
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                val forcedNonStop = settings?.alarmNonStop == true

                val checkedValue = if (forcedNonStop) true else nonStop

                Text("Herhaal alarm tot stop")

                Switch(
                    checked = checkedValue,
                    onCheckedChange = {
                        if (!forcedNonStop) {
                            viewModel.setNonStop(it)
                        }
                    },
                    enabled = !forcedNonStop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // verdeelt ruimte
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text("Alarm")

                Button(
                    onClick = {
                        launcher.launch(arrayOf("audio/*"))
                    },
                    modifier = Modifier.width(80.dp)
                ) {
                    Text(
                        "Bestand",
                        fontSize = 8.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alarmUri?.lastPathSegment ?: alarmSound ?: "geen bestand",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 10,
                overflow = TextOverflow.Clip
            )



            Spacer(modifier = Modifier.height(24.dp))


            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.updateColor("add_color", "#0288D1")
                    viewModel.updateColor("alarm_color", "#FF0000")
                    viewModel.updateColor("background_color", "#B3E5FC")
                    viewModel.updateAlarmSound("android.resource://com.example.ringer/raw/alarm")
                    viewModel.setSnoozeTime(5)
                    viewModel.setSound(true)


                }) {
                Text("Standaardwaarden")
            }


        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = currentColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { newColor ->
                // Sla de kleur op in je ViewModel / DataStore
                val color = Color(
                    android.graphics.Color.HSVToColor(
                        floatArrayOf(
                            newColor.hue,
                            newColor.saturation,
                            newColor.value
                        )
                    )
                )
                viewModel.updateColor(colorKey, "#%06X".format(0XFFFFFF and color.toArgb()))
            }
        )
    }


}
