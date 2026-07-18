package com.zenodotus.ringer.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.Task
import com.zenodotus.ringer.database.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application, private val repository: TaskRepository) :
    AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)


    //private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            loadTasks(userPrefs.getUsername())
        }
    }

    fun loadTasks(userName: String?) {
        if (userName == null) return

        viewModelScope.launch {
            repository.getTasks(userName).collect { gefilterdeLijst ->
                // WEG met alle extra berekeningen: vul gewoon de waarde
                // We gebruiken .toList() alleen om te zorgen dat Compose de wijziging herkent
                tasks = gefilterdeLijst.toList()
            }
        }
    }

    private val _snoozeAmounts = mutableMapOf<Int, Int>() // taskId -> amount

    fun currentSnoozeAmount(taskId: Int): Int = _snoozeAmounts[taskId] ?: 0

    fun incrementSnooze(taskId: Int) {
        _snoozeAmounts[taskId] = currentSnoozeAmount(taskId) + 1
    }

    fun resetSnooze(taskId: Int) {
        _snoozeAmounts[taskId] = 0
    }

    suspend fun addTask(task: Task): Task {
        return repository.addTask(task) // Zorg dat repository de nieuwe ID (Long) teruggeeft
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)

        }
    }

    suspend fun findTaskById(id: Int): Task? {
        return repository.getTaskById(id)
    }

    fun findTaskByIdFlow(id: Int): Flow<Task?> {
        return repository.getTaskByIdFlow(id)
    }

    fun markTaskAsDone(taskId: Int) {
        viewModelScope.launch {
            repository.markTaskAsDone(taskId)
        }
    }

    fun markTaskAsFailed(taskId: Int) {
        viewModelScope.launch {
            repository.markTaskAsFailed(taskId)
        }
    }

    fun snoozeTask(taskId: Int, minutes: Int) {
        viewModelScope.launch {
            repository.snoozeTask(taskId, minutes)
        }
    }


    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

}