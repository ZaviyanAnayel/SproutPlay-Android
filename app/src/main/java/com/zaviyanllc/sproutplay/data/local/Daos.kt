package com.zaviyanllc.sproutplay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM child_profiles ORDER BY createdAt ASC")
    fun observeAllProfiles(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles WHERE id = :profileId LIMIT 1")
    fun observeProfile(profileId: Long): Flow<ChildProfileEntity?>

    @Query("SELECT * FROM child_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: Long): ChildProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ChildProfileEntity)

    @Query("DELETE FROM child_profiles WHERE id = :profileId")
    suspend fun deleteProfile(profileId: Long)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM game_progress WHERE profileId = :profileId")
    fun observeProgressForProfile(profileId: Long): Flow<List<GameProgressEntity>>

    @Query("SELECT * FROM game_progress WHERE profileId = :profileId AND gameId = :gameId LIMIT 1")
    suspend fun getProgress(profileId: Long, gameId: String): GameProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: GameProgressEntity)

    @Query("SELECT * FROM unlocked_stickers WHERE profileId = :profileId ORDER BY unlockedAt DESC")
    fun observeStickersForProfile(profileId: Long): Flow<List<UnlockedStickerEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSticker(sticker: UnlockedStickerEntity)

    @Query("DELETE FROM game_progress WHERE profileId = :profileId")
    suspend fun deleteProgressForProfile(profileId: Long)

    @Query("DELETE FROM unlocked_stickers WHERE profileId = :profileId")
    suspend fun deleteStickersForProfile(profileId: Long)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: AppSettingsEntity)
}
