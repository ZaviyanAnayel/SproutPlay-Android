package com.zaviyanllc.sproutplay.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChildProfileEntity::class,
        GameProgressEntity::class,
        UnlockedStickerEntity::class,
        AppSettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SproutDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun progressDao(): ProgressDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SproutDatabase? = null

        fun getInstance(context: Context): SproutDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SproutDatabase::class.java,
                    "sproutplay_local.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
