package com.zenodotus.ringer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.EnumTrophy
import com.zenodotus.ringer.database.Trophy
import com.zenodotus.ringer.database.TrophyWithTaskName
import com.zenodotus.ringer.database.dao.TrophyDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrophyViewModel(application: Application, private val trophyDao: TrophyDao) :
    AndroidViewModel(application) {

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    private val userPrefs = UserPreferences(application)


    val trophies = MutableStateFlow<List<TrophyWithTaskName>>(emptyList())

    //private val trophyMap = EnumTrophy.entries.associateBy { it.title }

    init {
        viewModelScope.launch {
            val userName = userPrefs.getUsername() ?: return@launch

            trophyDao.getTrophiesOfUser(userName)
                .collect { list ->
                    trophies.value = list
                }
        }
    }

    fun loadTrophiesForUser(userName: String) {
        viewModelScope.launch {
            trophyDao.getTrophiesOfUser(userName)
                .collect { list ->
                    trophies.value = list
                }
        }
    }

    fun completedTasksFor(type: String): Int {
        return trophies.value
            .filter { it.trophyType == type }
            .maxOfOrNull { it.trophyValue } ?: 0
    }

    // Haalt voor elk type de volgende trophy en completedTasks op
    fun getNextTrophies(): List<Triple<TrophyWithTaskName, EnumTrophy, Int>> {
        return trophies.value.mapNotNull { trophy ->

            val possible = EnumTrophy.values()
                .filter { it.type == trophy.trophyType }
                .sortedBy { it.minValue }

            val next = possible.firstOrNull { trophy.trophyValue < it.minValue }

            if (next != null) {
                val progress = next.minValue - trophy.trophyValue

                Triple(trophy, next, progress)
            } else {
                null // deze reeks is al maxed out
            }
        }
    }

    fun awardTrophy(userName: String, taskId: Int, type: String, name: String, value: Int) {
        viewModelScope.launch {
            val trophy = Trophy(
                userName = userName,
                taskId = taskId,
                trophyType = type,
                trophyName = name,
                trophyValue = value
            )
            trophyDao.insert(trophy)
            onTrophyAdded()
        }
    }

    suspend fun getTrophy(userName: String, taskId: Int, type: String): Trophy? {
        return trophyDao.getTrophy(userName, taskId, type)
    }

    fun getEnumTrophy(taskId: Int): EnumTrophy? {
        val trophy = trophies.value.firstOrNull { it.taskId == taskId }
        return trophy?.let { t ->
            EnumTrophy.entries.firstOrNull { it.typeName == t.trophyName }
        }
    }

    fun updateTrophy(trophy: TrophyWithTaskName) {
        viewModelScope.launch {
            val newValue = trophy.trophyValue + 1

            val enumTrophy = EnumTrophy.findTrophy(
                trophy.trophyType,
                newValue
            )
            if (trophy.taskId != null) {
                trophyDao.updateTrophy(
                    trophy.userName,
                    trophy.taskId,
                    enumTrophy.name,
                    enumTrophy.type
                )
            } else {
                trophyDao.updateTrophy(
                    trophy.userName,
                    enumTrophy.name,
                    enumTrophy.type
                )

            }

        }
    }


    fun onTrophyAdded() {
        _refreshTrigger.value += 1
    }

    fun progress(value: Int, trophy: EnumTrophy): Float {
        val range = trophy.maxValue - trophy.minValue
        val current = (value - trophy.minValue).coerceAtLeast(0)
        return (current.toFloat() / range).coerceIn(0f, 1f)
    }

    fun tasksRemaining(value: Int, trophy: EnumTrophy): Int {
        return (trophy.maxValue - value).coerceAtLeast(0)
    }

    fun nextTrophy(type: String, completedTasks: Int): EnumTrophy? {
        return EnumTrophy.entries
            .filter { it.type == type }
            .firstOrNull { completedTasks < it.maxValue }
    }

    fun deleteTrophy(taskId: Int) {
        viewModelScope.launch {
            trophyDao.deleteTrophy(taskId)
        }

    }
}