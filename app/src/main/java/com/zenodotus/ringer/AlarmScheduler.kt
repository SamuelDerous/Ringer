package com.zenodotus.ringer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmScheduler(private val context: Context) {

    fun scheduleAlarm(
        taskId: Int,
        triggerAtMillis: Long,
        title: String,
        message: String,
        frequency: String,
        frequencyValue: Int
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("frequencyType", frequency)
            putExtra("frequencyValue", frequencyValue)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            //Log.w("app", "Gewoon alarm ingepland wegens ontbrekende exact-alarmtoestemming")
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            //Log.d("app", "Exact alarm ingepland voor: ${Date(triggerAtMillis)}")
        }
    }


    fun cancelAlarm(taskId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId, // dezelfde requestCode als bij het zetten!
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

