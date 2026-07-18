package com.zenodotus.ringer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USERNAME_KEY = stringPreferencesKey("username")
        val LOGGED_IN_KEY = booleanPreferencesKey("logged_in")
    }

    val usernameFlow = context.dataStore.data.map { prefs ->
        prefs[USERNAME_KEY]
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    suspend fun getUsername(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[USERNAME_KEY]
    }

    val isLoggedInFlow = context.dataStore.data.map { prefs ->
        prefs[LOGGED_IN_KEY] ?: false
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN_KEY] = loggedIn
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.dataStore.data.first() // suspend function
        return prefs[LOGGED_IN_KEY] ?: false
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
