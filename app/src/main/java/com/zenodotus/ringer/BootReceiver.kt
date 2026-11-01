package com.zenodotus.ringer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.AppDatabase
import com.zenodotus.ringer.database.repositories.TaskRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val username = UserPreferences(context).getUsername()
                    ?: return@launch

                val db = AppDatabase.getDatabase(context)

                val repository = TaskRepository(
                    db.taskDao(),
                    AlarmScheduler(context)
                )

                repository.triggerAlarms(repository.getTasks(username)).first();

            } finally {
                pendingResult.finish()
            }
        }
    }
}