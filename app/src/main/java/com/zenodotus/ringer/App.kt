package com.zenodotus.ringer

import android.app.Application
import com.zenodotus.ringer.database.AppDatabase
import com.zenodotus.ringer.database.repositories.TaskRepository
import com.zenodotus.ringer.viewmodels.ColorSettingsViewModel
import com.zenodotus.ringer.viewmodels.LoginViewModel
import com.zenodotus.ringer.viewmodels.MedalViewModel
import com.zenodotus.ringer.viewmodels.PermissionsViewModel
import com.zenodotus.ringer.viewmodels.TaskViewModel
import com.zenodotus.ringer.viewmodels.TrophyViewModel
import com.zenodotus.ringer.viewmodels.UserSettingsViewModel

class MyApp : Application() {

    lateinit var authViewModel: LoginViewModel
    lateinit var medalViewModel: MedalViewModel

    lateinit var trophyViewModel: TrophyViewModel


    lateinit var colorSettingsViewModel: ColorSettingsViewModel

    lateinit var permissionsViewModel: PermissionsViewModel

    lateinit var taskViewModel: TaskViewModel

    lateinit var masterSettingsViewModel: UserSettingsViewModel


    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(this)
        medalViewModel =
            MedalViewModel(
                this,
                db.medalDao(),
                TaskRepository(db.taskDao(), AlarmScheduler(this))
            ) // Gebruik `this` in plaats van `applicationContext`
        colorSettingsViewModel = ColorSettingsViewModel(this)
        permissionsViewModel = PermissionsViewModel(this)
        taskViewModel = TaskViewModel(this, TaskRepository(db.taskDao(), AlarmScheduler(this)))
        trophyViewModel = TrophyViewModel(this, db.trophyDao())
        masterSettingsViewModel = UserSettingsViewModel(this, db.userSettingsDao())
        authViewModel =
            LoginViewModel(this, db, trophyViewModel, taskViewModel, masterSettingsViewModel)
    }


}
