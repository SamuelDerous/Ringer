package com.zenodotus.ringer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenodotus.ringer.database.Medal
import com.zenodotus.ringer.database.dao.MedalDao
import com.zenodotus.ringer.database.repositories.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedalViewModel(
    application: Application,
    private val medalDao: MedalDao,
    private val repo: TaskRepository
) : AndroidViewModel(application) {

    private val _dayMedals = MutableStateFlow(0)
    val dayMedals: StateFlow<Int> = _dayMedals

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    private val _medalsOfTask = MutableStateFlow(0)
    val medalsOfTask: StateFlow<Int> = _medalsOfTask

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    fun medalsToday(userName: String, today: LocalDate) {
        viewModelScope.launch {
            val count = medalDao.countTodaysMedals(userName, today)
            _dayMedals.value = count
        }
    }

    fun medalsPerTask(userName: String, taskId: Int) {
        viewModelScope.launch {
            val count = medalDao.countMedalsPerTask(userName, taskId)
            _medalsOfTask.value = count
        }
    }

    fun getStreaks(userName: String, taskId: Int) {
        viewModelScope.launch {
            val medals = medalDao.getMedalsPerTask(userName, taskId)
            val task = repo.getTaskById(taskId)
            val calculated = calculateStreak(medals, task?.frequency, task?.frequencyValue)
            _streak.value = calculated
        }
    }

    fun calculateStreak(medals: List<Medal>, frequency: String?, freqValue: Int? = 0): Int {
        if (medals.isEmpty()) return 0

        var streak = 1 // start bij 1 omdat we minstens 1 medal hebben
        var previousDate = medals[0].dateEarned
        for (i in 1 until medals.size) { // start vanaf 1, want 0 is al in streak
            val currentDate = medals[i].dateEarned

            // Bereken hoeveel tijd er exact tussen moet zitten
            val expectedDate = if (freqValue != null && freqValue > 0) {
                previousDate.minusDays(freqValue.toLong())
            } else {
                when (frequency) {
                    "Dagelijks" -> previousDate.minusDays(1)
                    "Wekelijks" -> previousDate.minusWeeks(1)
                    "Maandelijks" -> previousDate.minusMonths(1)
                    "Jaarlijks" -> previousDate.minusYears(1)
                    else -> null
                }
            }
            if (expectedDate != null && currentDate == expectedDate) {
                streak++
            } else {
                break
            }

            previousDate = currentDate
        }
        return streak
    }


    fun awardMedal(userName: String, taskId: Int) {
        viewModelScope.launch {
            val medal = Medal(userName = userName, taskId = taskId)
            medalDao.insert(medal)
            onMedalAdded()

        }
    }

    fun onMedalAdded() {
        _refreshTrigger.value += 1
    }


}