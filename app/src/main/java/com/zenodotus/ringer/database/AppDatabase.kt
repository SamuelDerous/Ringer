package com.zenodotus.ringer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zenodotus.ringer.data.UserPreferences
import com.zenodotus.ringer.database.dao.MedalDao
import com.zenodotus.ringer.database.dao.TaskDao
import com.zenodotus.ringer.database.dao.TrophyDao
import com.zenodotus.ringer.database.dao.UserDao
import com.zenodotus.ringer.database.dao.UserSettingsDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Database(
    entities = [User::class, UserSetting::class, UserBadge::class, Task::class, Medal::class,
        Trophy::class],
    version = 20
)


@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun medalDao(): MedalDao

    abstract fun trophyDao(): TrophyDao
    //abstract fun userSettingDao(): UserSettingDao

    abstract fun userSettingsDao(): UserSettingsDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_setting ADD COLUMN alarmNonStop INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE?.close()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "userDatabase"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            GlobalScope.launch {
                                val userPrefs = UserPreferences(context.applicationContext)
                                userPrefs.clear()
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            GlobalScope.launch {
                                val userPrefs = UserPreferences(context.applicationContext)
                                userPrefs.clear()
                            }
                        }

                    })

                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }

}