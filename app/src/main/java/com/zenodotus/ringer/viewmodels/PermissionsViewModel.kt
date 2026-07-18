package com.zenodotus.ringer.viewmodels

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsViewModel(
    private val app: Application
) : AndroidViewModel(app) {

    private val _notificationsGranted = MutableStateFlow(false)
    val notificationsGranted = _notificationsGranted.asStateFlow()

    private val _batteryExempt = MutableStateFlow(false)
    val batteryExempt = _batteryExempt.asStateFlow()

    fun refresh() {
        _notificationsGranted.value = areNotificationsEnabled()
        _batteryExempt.value = isIgnoringBatteryOptimizations()
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat
            .from(app)
            .areNotificationsEnabled()
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(app.packageName)
        } else true

    }
}