package com.zenodotus.ringer.database.repositories

import android.util.Log
import com.zenodotus.ringer.AlarmScheduler
import com.zenodotus.ringer.computeNextTriggerMillis
import com.zenodotus.ringer.database.Task
import com.zenodotus.ringer.database.dao.TaskDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao, private val alarmScheduler: AlarmScheduler) {

    suspend fun updateTask(updatedTask: Task) {
        val oldTask = taskDao.getTaskById(updatedTask.taskId)
            ?: error("Task with id ${updatedTask.taskId} does not exist")

        val alarmNeedsUpdate = hasAlarmChanged(oldTask, updatedTask)

        if (alarmNeedsUpdate) {
            alarmScheduler.cancelAlarm(oldTask.taskId)
            if (!updatedTask.archived) {
                val triggerAtMillis = computeNextTriggerMillis(
                    lastTriggerMillis = null,
                    updatedTask.frequency,
                    updatedTask.frequencyValue,
                    updatedTask.day,
                    updatedTask.time
                )
                alarmScheduler.scheduleAlarm(
                    updatedTask.taskId, triggerAtMillis, updatedTask.task, "Updated Task",
                    updatedTask.frequency, updatedTask.frequencyValue
                )
            }
        }

        taskDao.updateTask(updatedTask)
    }

    fun getTasks(userName: String): Flow<List<Task>> {
        return taskDao.getTasks(userName).map { dbTasks ->
            // 1. Filter de gearchiveerde taken eruit voor de UI
            val actieveTaken = dbTasks

            // 2. Behandel de alarm logica alleen voor de relevante taken
            actieveTaken.forEach { task ->
                val nextTrigger = computeNextTriggerMillis(
                    lastTriggerMillis = task.lastCompleted,
                    freqType = task.frequency,
                    freqValue = task.frequencyValue,
                    dayOfWeek = task.day,
                    time = task.time
                )

                if (task.frequency != "Eenmalig") {
                    addAlarm(task, nextTrigger)
                }
            }

            // 3. Geef de GEFILTERDE lijst terug aan de ViewModel
            actieveTaken
        }
    }

    suspend fun addTask(task: Task): Task {
        val id = taskDao.insert(task)
        val taskWithId = task.copy(taskId = id.toInt())
        addAlarm(taskWithId)
        return taskWithId
    }

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    fun getTaskByIdFlow(id: Int): Flow<Task?> {
        return taskDao.getTaskByIdFlow(id)
    }

    suspend fun deleteTask(taskId: Int): Task? {
        val task = getTaskById(taskId)
        if (task != null) {
            alarmScheduler.cancelAlarm(taskId)
            taskDao.deleteTask(task)
            return task
        } else {
            return null
        }
    }

    private fun hasAlarmChanged(old: Task, new: Task): Boolean {
        return old.day != new.day ||
                old.time != new.time ||
                old.frequency != new.frequency ||
                old.frequencyValue != new.frequencyValue

    }

    suspend fun markTaskAsDone(taskId: Int) {
        val task = taskDao.getTaskById(taskId) ?: return
        val updated = task.copy(
            completedCount = task.completedCount + 1,
            lastCompleted = System.currentTimeMillis()
        )

        taskDao.updateTask(updated)
    }

    suspend fun markTaskAsFailed(taskId: Int) {
        val task = taskDao.getTaskById(taskId) ?: return
        val updated = task.copy(
            failedCount = task.failedCount + 1,
            lastCompleted = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    suspend fun snoozeTask(taskId: Int, minutes: Int) {
        val task = taskDao.getTaskById(taskId) ?: return
        alarmScheduler.cancelAlarm(taskId)
        val newTriggerTime = System.currentTimeMillis() + minutes * 60 * 1000
        addAlarm(task, newTriggerTime, true)
    }

    suspend fun rescheduleAlarm(taskid: Int) {
        val task = taskDao.getTaskById(taskid) ?: return
    }

    fun addAlarm(task: Task, lastTriggerMillis: Long? = null, snooze: Boolean? = false) {
        val baseTrigger = lastTriggerMillis
        var nextTrigger = baseTrigger ?: System.currentTimeMillis()
        Log.d("app", "baseTrigger: $baseTrigger")
        if (snooze != true) {
            nextTrigger = computeNextTriggerMillis(
                lastTriggerMillis = baseTrigger,
                freqType = task.frequency,
                freqValue = task.frequencyValue,
                dayOfWeek = task.day,
                time = task.time
            )
        }
        alarmScheduler.scheduleAlarm(
            taskId = task.taskId,
            triggerAtMillis = nextTrigger,
            title = "Alarm",
            message = task.task,
            frequency = task.frequency,
            frequencyValue = task.frequencyValue
        )
    }



}