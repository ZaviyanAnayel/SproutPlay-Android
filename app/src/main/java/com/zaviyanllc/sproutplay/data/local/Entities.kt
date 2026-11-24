package com.zaviyanllc.sproutplay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val age: Int,
    val avatarId: String,
    val totalStars: Int = 0,
    val streakDays: Int = 1,
    val lastPlayedEpochDay: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "game_progress",
    primaryKeys = ["profileId", "gameId"],
)
data class GameProgressEntity(
    val profileId: Long,
    val gameId: String,
    val starsEarned: Int = 0,
    val highScore: Int = 0,
    val timesPlayed: Int = 0,
    val lastTierIndex: Int = 0,
    val lastPlayedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "unlocked_stickers",
    primaryKeys = ["profileId", "stickerId"],
)
data class UnlockedStickerEntity(
    val profileId: Long,
    val stickerId: String,
    val unlockedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val activeProfileId: Long? = null,
    val soundEnabled: Boolean = true,
    val voicePromptsEnabled: Boolean = true,
    val musicCuesEnabled: Boolean = true,
    val dailyTimeLimitMinutes: Int = 30,
    val secondsUsedToday: Int = 0,
    val lastUsageEpochDay: Long = 0L,
)
