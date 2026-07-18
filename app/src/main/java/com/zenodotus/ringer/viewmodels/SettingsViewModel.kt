package com.zenodotus.ringer.viewmodels
// SettingsViewModel.kt
// SettingsViewModel.kt

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenodotus.ringer.data.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// DataStore extensie op Context

class ColorSettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val context = app.applicationContext

    // Keys
    companion object Keys {
        val ALARM_COLOR = stringPreferencesKey("alarm_color")
        val BACKGROUND_COLOR = stringPreferencesKey("background_color")
        val ADD_COLOR = stringPreferencesKey("add_color")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound")

        val SNOOZE_TIME = intPreferencesKey("snooze_time")

        val SOUND = booleanPreferencesKey("sound_enabled")

        val NONSTOP = booleanPreferencesKey("nonstop")
    }

    // -------------------------
    // Flows voor Compose UI
    // ---------
    //----------------
    val soundEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SOUND] ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val nonStop: StateFlow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[NONSTOP] ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)


    val snoozeTime: StateFlow<Int> = context.dataStore.data
        .map { prefs -> prefs[SNOOZE_TIME] ?: 1 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val alarmColor: StateFlow<String> = context.dataStore.data
        .map { prefs -> prefs[ALARM_COLOR] ?: "#FF0000" } // default rood
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#FF0000")

    val addColor: StateFlow<String> = context.dataStore.data
        .map { prefs -> prefs[ADD_COLOR] ?: "#A9CFF1" } // default rood
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#A9CFF1")

    val backgroundColor: StateFlow<String> = context.dataStore.data
        .map { prefs -> prefs[BACKGROUND_COLOR] ?: "#E6F0FA" } // default rood
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#E6F0FA")


    val alarmSound: StateFlow<String?> = context.dataStore.data
        .map { prefs -> prefs[ALARM_SOUND] }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // -------------------------
    // Opslaan methodes
    // -------------------------
    fun updateColor(key: String, colorHex: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[stringPreferencesKey(key)] = colorHex
            }
        }
    }

    fun getColor(key: String): Color {
        val hex = when (key) {
            "alarm_color" -> alarmColor.value
            "background_color" -> backgroundColor.value
            "add_color" -> addColor.value
            else -> "#000000"
        }

        return try {
            Color(hex.toColorInt())
        } catch (e: Exception) {
            Color.Black
        }
    }

    fun updateAlarmSound(soundUri: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[ALARM_SOUND] = soundUri
            }
        }
    }

    fun setSnoozeTime(value: Int) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[SNOOZE_TIME] = value
            }
        }
    }

    fun setSound(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[SOUND] = value
            }
        }
    }

    fun setNonStop(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[NONSTOP] = value
            }
        }
    }
}

