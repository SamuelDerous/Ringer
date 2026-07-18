package com.zenodotus.ringer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.UserSetting
import com.zenodotus.ringer.database.dao.UserSettingsDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserSettingsViewModel(
    application: Application,
    private val userSettingsDao: UserSettingsDao
) : AndroidViewModel(application) {

    val userPrefs = UserPreferences(application)

    private val _userSettings = MutableStateFlow<UserSetting?>(null)
    val userSettings: StateFlow<UserSetting?> = _userSettings

    init {
        viewModelScope.launch {
            val userName = userPrefs.getUsername()
            if (userName != null) {
                // fetch van DB of default settings maken
                val settingsFromDb = userSettingsDao.getUserSettings(userName)
                _userSettings.value = settingsFromDb ?: createDefaultSettings(userName)
            }
        }
    }

    fun loadSettingsForMasterUser(userName: String) {
        viewModelScope.launch {
            val settingsFromDb = userSettingsDao.getUserSettings(userName)
            _userSettings.value = settingsFromDb ?: createDefaultSettings(userName)
        }
    }

    private suspend fun createDefaultSettings(userName: String): UserSetting {
        val default = UserSetting(userName)
        userSettingsDao.updateSettings(default)
        return default
    }

    // Eén update-functie die alles tegelijk doet
    fun updateSettings(newSettings: UserSetting) {
        viewModelScope.launch {
            userSettingsDao.updateSettings(newSettings)  // DB schrijven
            _userSettings.value = newSettings           // Compose updaten
        }
    }

    // Helper om velden afzonderlijk te updaten
    fun updateField(field: String, value: Any) {
        val current = _userSettings.value ?: return
        val updated = when (field) {
            "settings" -> current.copy(settings = value as Boolean)
            "intrusionAlert" -> current.copy(intrusionAlert = value as Boolean)
            "persist" -> current.copy(persist = value as Boolean)
            "timeBetween" -> current.copy(timeBetween = value as Int)
            "amountBetween" -> current.copy(amountBetween = value as Int)
            "alarmNonStop" -> current.copy(alarmNonStop = value as Boolean)
            else -> current
        }
        updateSettings(updated) // gebruik altijd de centrale update
    }
}



