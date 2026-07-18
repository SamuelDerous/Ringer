package com.zenodotus.ringer

import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil3.compose.rememberAsyncImagePainter
import com.zenodotus.ringer.database.Task
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import com.zenodotus.ringer.viewmodels.MedalViewModel
import com.zenodotus.ringer.viewmodels.TaskViewModel
import com.zenodotus.ringer.viewmodels.TrophyViewModel
import com.zenodotus.ringer.viewmodels.UserSettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters


class AlarmActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by lazy {
        (application as MyApp).taskViewModel
    }
    private val colorSettingsViewModel: ColorSettingsViewModel by lazy {
        (application as MyApp).colorSettingsViewModel
    }

    private val medalViewModel: MedalViewModel by lazy {
        (application as MyApp).medalViewModel
    }

    private val trophyViewModel: TrophyViewModel by lazy {
        (application as MyApp).trophyViewModel
    }

    private val masterSettingsViewModel: UserSettingsViewModel by lazy {
        (application as MyApp).masterSettingsViewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        val taskId = intent.getIntExtra("taskId", 0)
        var amount = intent.getIntExtra("amount", 0)
        lifecycleScope.launch {
            // 1️⃣ Wacht tot de userSettings geladen zijn
            val userSettings = masterSettingsViewModel.userSettings.first { it != null }

            // 2️⃣ Zoek de task
            val task = viewModel.findTaskById(taskId)
            if (task == null) {
                finish()
                return@launch
            }

            // 3️⃣ Alles wat afhankelijk is van userSettings en task gebeurt hieronder
            // Bijvoorbeeld snooze, done, dismiss

            fun handleDone() {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(taskId)
                viewModel.markTaskAsDone(task.taskId)
                medalViewModel.awardMedal(task.assignedToUserName, task.taskId)
                medalViewModel.onMedalAdded()
                lifecycleScope.launch {
                    for (trophy in trophyViewModel.trophies.value) {
                        if (trophy.taskId == null || trophy.taskId == task.taskId) {
                            trophyViewModel.updateTrophy(trophy)
                        }
                    }
                }
                AlarmSoundManager.stop()
                viewModel.resetSnooze(task.taskId)
                finish()
            }

            fun handleSnooze() {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(taskId)
                val snoozeTime =
                    if (userSettings != null && (userSettings.settings && userSettings.timeBetween > 0))
                        userSettings.timeBetween
                    else
                        colorSettingsViewModel.snoozeTime.value
                viewModel.snoozeTask(task.taskId, snoozeTime)
                viewModel.incrementSnooze(task.taskId)
                AlarmSoundManager.stop()
                finish()
            }

            fun handleDismiss() {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(taskId)
                viewModel.markTaskAsFailed(task.taskId)
                viewModel.resetSnooze(task.taskId)
                AlarmSoundManager.stop()
                finish()
            }

            // 4️⃣ Hier kan je je UI initialiseren of een fragment / view tonen
            // Als je een composable gebruikt via setContent:
            setContent {
                AlarmScreen(
                    task = task,
                    colorSettingsViewModel = colorSettingsViewModel,
                    viewModel = viewModel,
                    onDone = ::handleDone,
                    onSnooze = ::handleSnooze,
                    onDismiss = ::handleDismiss,
                    userSettingsViewModel = masterSettingsViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val taskId = intent.getIntExtra("taskId", 0)
        lifecycleScope.launch {
            val task = viewModel.findTaskById(taskId)
            val userSettings = masterSettingsViewModel.userSettings.first { it != null }
            if (task == null) {
                Log.e("AlarmActivity", "Task niet gevonden: $taskId")
                finish()
                return@launch
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(taskId)
            val snoozeTime =
                if (userSettings != null && (userSettings.settings && userSettings.timeBetween > 0))
                    userSettings.timeBetween
                else
                    colorSettingsViewModel.snoozeTime.value
            viewModel.snoozeTask(task.taskId, snoozeTime)
            viewModel.incrementSnooze(task.taskId)
        }
        AlarmSoundManager.stop()
    }

    // 2. Optioneel: Wordt aangeroepen als de gebruiker op de Home-knop drukt
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val taskId = intent.getIntExtra("taskId", 0)
        lifecycleScope.launch {
            val task = viewModel.findTaskById(taskId)
            val userSettings = masterSettingsViewModel.userSettings.first { it != null }
            if (task == null) {
                Log.e("AlarmActivity", "Task niet gevonden: $taskId")
                finish()
                return@launch
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(taskId)
            val snoozeTime =
                if (userSettings != null && (userSettings.settings && userSettings.timeBetween > 0))
                    userSettings.timeBetween
                else
                    colorSettingsViewModel.snoozeTime.value
            viewModel.snoozeTask(task.taskId, snoozeTime)
            viewModel.incrementSnooze(task.taskId)
        }
        AlarmSoundManager.stop()
    }

    // 3. Zorg dat de Back-button ook het geluid stopt
    override fun onBackPressed() {
        super.onBackPressed()
        val taskId = intent.getIntExtra("taskId", 0)
        lifecycleScope.launch {
            val task = viewModel.findTaskById(taskId)
            val userSettings = masterSettingsViewModel.userSettings.first { it != null }
            if (task == null) {
                Log.e("AlarmActivity", "Task niet gevonden: $taskId")
                finish()
                return@launch
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(taskId)
            val snoozeTime =
                if (userSettings != null && (userSettings.settings && userSettings.timeBetween > 0))
                    userSettings.timeBetween
                else
                    colorSettingsViewModel.snoozeTime.value
            viewModel.snoozeTask(task.taskId, snoozeTime)
            viewModel.incrementSnooze(task.taskId)
        }
        AlarmSoundManager.stop()
    }
}


@Composable
fun AlarmScreen(
    task: Task?,
    viewModel: TaskViewModel,
    colorSettingsViewModel: ColorSettingsViewModel,
    userSettingsViewModel: UserSettingsViewModel,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showMedal by rememberSaveable { mutableStateOf(false) }
    val userSettings by userSettingsViewModel.userSettings.collectAsState()
    LaunchedEffect(showMedal) {
        if (showMedal) {
            delay(1200)
            onDone()
        }
    }
    if (task != null && !task.archived) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorSettingsViewModel.getColor("alarm_color") // alarmkleur
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                if (!task.picto.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter("file:///android_asset/pictos/${task.picto}"),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = "⏰ ${task.task}",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            showMedal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Done")
                    }

                    Button(
                        onClick = {
                            onSnooze()
                        }, // bv. snooze 10 min
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
                    ) {
                        Text("Snooze")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        enabled = userSettings?.let { s ->
                            // knop mag alleen klikken als:
                            // 1. master settings aan maar persist uit
                            // 2. en amount >= amountBetween
                            !(s.settings && s.persist) && (viewModel.currentSnoozeAmount(task.taskId) >= s.amountBetween)
                        } ?: false
                    ) {// als settings nog null is, knop uit
                        Text("Stop")
                    }
                }
                if (showMedal) {
                    MedalAnimation(visible = showMedal) {

                    }

                }
            }
        }
    }


}

fun computeNextTriggerMillis(
    lastTriggerMillis: Long? = null,
    freqType: String,
    freqValue: Int,
    dayOfWeek: Int? = null, // 1 = maandag ... 7 = zondag of maand/jaar index
    time: LocalTime? = null
): Long {
    val zone = ZoneId.systemDefault()
    val now = LocalDateTime.now()
    Log.d("app", "day of week: $dayOfWeek")
    // 1. Basisdatum
    var base = lastTriggerMillis?.let {
        Log.d("app", "lastrigger gebruikt!")
        Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime()
    } ?: dayOfWeek?.let {
        Log.d("app", "dayofweek gebruikt!")
        now.with(ChronoField.DAY_OF_WEEK, it.toLong())
    } ?: now

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

    Log.d("app", "test: ${base.format(formatter)}")
    // 2. Tijd instellen
    if (time != null) {
        base = base.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
    }

    // 3. Day-of-week / maand / jaar aanpassen
    if (dayOfWeek != null) {
        when (freqType) {
            "Eenmalig", "Wekelijks" -> {
                val todayDow = base.dayOfWeek.value
                var diff = (dayOfWeek - todayDow).let { if (it < 0) it + 7 else it }
                // Alleen naar volgende week als de tijd van vandaag al gepasseerd is
                if (diff == 0 && base.isBefore(now)) diff = 7
                base = base.plusDays(diff.toLong())
            }

            "Maandelijks" -> {
                var day = base.dayOfMonth
                base = base.plusMonths(1)

                val maxDay = base.toLocalDate().lengthOfMonth()
                base = base.withDayOfMonth(day.coerceAtMost(maxDay))
            }

            "Jaarlijks" -> {
                base = base.plusYears(1)
            }
        }
    }

    // 4. Aangepast = freqValue dagen
    if (freqType == "Aangepast") {
        Log.d("app", "Base Before: ${base.format(formatter)}")
        if (base.isBefore(now)) {
            base = base.plusDays(freqValue.coerceAtLeast(1).toLong())
        }
        Log.d("app", "Base: ${base.format(formatter)}")
        return base.atZone(zone).toInstant().toEpochMilli()
    }

    // 5. Interval toepassen als base < now EN de trigger voor vandaag al gepasseerd is
    when (freqType) {
        "Dagelijks" -> while (base.isBefore(now)) base = base.plusDays(1)
        "Wekelijks" -> while (base.isBefore(now)) base = base.plusWeeks(1)
        "Maandelijks" -> while (base.isBefore(now)) base = base.plusMonths(1)
        "Jaarlijks" -> while (base.isBefore(now)) base = base.plusYears(1)
    }

    return base.atZone(zone).toInstant().toEpochMilli()
}



