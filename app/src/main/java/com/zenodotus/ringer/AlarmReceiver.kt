package com.zenodotus.ringer

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.zenodotus.ringer.data.dataStore
import com.zenodotus.ringer.database.AppDatabase
import com.zenodotus.ringer.database.repositories.TaskRepository
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: "Alarm"
        val message = intent.getStringExtra("message") ?: "Het is tijd!"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = context.dataStore.data.first()
                val soundEnabled = data[ColorSettingsViewModel.Keys.SOUND] ?: true
                val soundString = data[ColorSettingsViewModel.Keys.ALARM_SOUND]
                val nonStop = data[ColorSettingsViewModel.Keys.NONSTOP] ?: true

                val soundUri: Uri = try {
                    if (!soundString.isNullOrEmpty()) {
                        val parsedUri = soundString.toUri()
                        context.contentResolver.openInputStream(parsedUri)?.close()
                        parsedUri
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    }
                } catch (e: SecurityException) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }

                val db = AppDatabase.getDatabase(context)
                val repository = TaskRepository(db.taskDao(), AlarmScheduler(context))
                val task = repository.getTaskById(taskId)

                if (task != null && task.frequency != "Eenmalig") {
                    repository.addAlarm(task, System.currentTimeMillis(), false)
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channelId = "alarm_ch_$taskId"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Alarmen",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        setSound(null, null)
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("taskId", taskId)
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    taskId,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(pendingIntent, true)
                    .build()

                notificationManager.notify(taskId, notification)
                context.startActivity(activityIntent)

                if (soundEnabled) {
                    withContext(Dispatchers.Main) {

                        AlarmSoundManager.stop()

                        val mediaPlayer = MediaPlayer().apply {
                            setDataSource(context, soundUri)

                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )

                            isLooping = nonStop
                            prepare()
                            start()
                        }

                        AlarmSoundManager.mediaPlayer = mediaPlayer
                    }
                }

            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Fout: ${e.message}")
            }
        }
    }
}

