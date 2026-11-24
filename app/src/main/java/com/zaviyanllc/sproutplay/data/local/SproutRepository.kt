package com.zaviyanllc.sproutplay.data.local

import com.zaviyanllc.sproutplay.domain.model.GameCatalog
import com.zaviyanllc.sproutplay.domain.model.StickerCatalog
import com.zaviyanllc.sproutplay.domain.model.StickerSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SproutRepository(
    private val profileDao: ProfileDao,
    private val progressDao: ProgressDao,
    private val settingsDao: SettingsDao,
) {
    val allProfiles: Flow<List<ChildProfileEntity>> = profileDao.observeAllProfiles()

    val settings: Flow<AppSettingsEntity> = settingsDao.observeSettings().map {
        it ?: AppSettingsEntity()
    }

    fun observeProgress(profileId: Long): Flow<List<GameProgressEntity>> =
        progressDao.observeProgressForProfile(profileId)

    fun observeStickers(profileId: Long): Flow<List<UnlockedStickerEntity>> =
        progressDao.observeStickersForProfile(profileId)

    suspend fun createAndActivateProfile(
        name: String,
        age: Int,
        avatarId: String,
    ): Long {
        val today = System.currentTimeMillis() / 86_400_000L
        val cleanName = name.trim().ifEmpty { "Little Explorer" }
        val newId = profileDao.insertProfile(
            ChildProfileEntity(
                name = cleanName,
                age = age.coerceIn(3, 11),
                avatarId = avatarId,
                totalStars = 0,
                streakDays = 1,
                lastPlayedEpochDay = today,
            ),
        )
        val current = settingsDao.getSettings() ?: AppSettingsEntity()
        settingsDao.upsertSettings(current.copy(activeProfileId = newId))
        return newId
    }

    suspend fun switchActiveProfile(profileId: Long) {
        val current = settingsDao.getSettings() ?: AppSettingsEntity()
        settingsDao.upsertSettings(current.copy(activeProfileId = profileId))
    }

    suspend fun updateProfile(profile: ChildProfileEntity) {
        profileDao.updateProfile(profile.copy(age = profile.age.coerceIn(3, 11)))
    }

    suspend fun deleteProfile(profileId: Long) {
        progressDao.deleteProgressForProfile(profileId)
        progressDao.deleteStickersForProfile(profileId)
        profileDao.deleteProfile(profileId)
        val remaining = profileDao.observeAllProfiles().first()
        val current = settingsDao.getSettings() ?: AppSettingsEntity()
        if (current.activeProfileId == profileId) {
            settingsDao.upsertSettings(current.copy(activeProfileId = remaining.firstOrNull()?.id))
        }
    }

    suspend fun updateSettings(transform: (AppSettingsEntity) -> AppSettingsEntity) {
        val current = settingsDao.getSettings() ?: AppSettingsEntity()
        settingsDao.upsertSettings(transform(current))
    }

    suspend fun addPlaySeconds(deltaSeconds: Int) {
        if (deltaSeconds <= 0) return
        val today = System.currentTimeMillis() / 86_400_000L
        val current = settingsDao.getSettings() ?: AppSettingsEntity()
        val resetToday = current.lastUsageEpochDay != today
        val newSeconds = (if (resetToday) 0 else current.secondsUsedToday) + deltaSeconds
        settingsDao.upsertSettings(
            current.copy(
                secondsUsedToday = newSeconds,
                lastUsageEpochDay = today,
            ),
        )
    }

    suspend fun recordGameResult(
        profileId: Long,
        gameId: String,
        starsEarnedThisRun: Int,
        score: Int,
        tierIndex: Int,
    ): List<StickerSpec> {
        val existing = progressDao.getProgress(profileId, gameId)
        val previousStars = existing?.starsEarned ?: 0
        val bestStars = maxOf(previousStars, starsEarnedThisRun.coerceIn(1, 3))
        val bestScore = maxOf(existing?.highScore ?: 0, score)
        val deltaStars = (bestStars - previousStars).coerceAtLeast(0) +
            if (gameId == "arc_10") 2 else 1 // encouraging effort star on every completion!

        progressDao.upsertProgress(
            GameProgressEntity(
                profileId = profileId,
                gameId = gameId,
                starsEarned = bestStars,
                highScore = bestScore,
                timesPlayed = (existing?.timesPlayed ?: 0) + 1,
                lastTierIndex = tierIndex,
                lastPlayedAt = System.currentTimeMillis(),
            ),
        )

        val profile = profileDao.getProfileById(profileId) ?: return emptyList()
        val today = System.currentTimeMillis() / 86_400_000L
        val nextStreak = when {
            profile.lastPlayedEpochDay == today -> profile.streakDays
            profile.lastPlayedEpochDay == today - 1L -> profile.streakDays + 1
            else -> 1
        }
        val updatedTotalStars = profile.totalStars + deltaStars
        profileDao.updateProfile(
            profile.copy(
                totalStars = updatedTotalStars,
                streakDays = nextStreak,
                lastPlayedEpochDay = today,
            ),
        )

        // Check newly unlocked stickers
        val allProgress = progressDao.observeProgressForProfile(profileId).first()
        val alreadyUnlocked = progressDao.observeStickersForProfile(profileId).first()
            .map { it.stickerId }
            .toSet()

        val newlyUnlocked = mutableListOf<StickerSpec>()
        for (sticker in StickerCatalog.allStickers) {
            if (sticker.id in alreadyUnlocked) continue
            val meetsStars = sticker.requiredTotalStars > 0 && updatedTotalStars >= sticker.requiredTotalStars
            val meetsCat = sticker.requiredCategory != null && run {
                val catIds = GameCatalog.byCategory(sticker.requiredCategory).map { it.id }.toSet()
                allProgress.count { it.gameId in catIds && it.timesPlayed > 0 } >= sticker.requiredCategoryGames
            }
            if (meetsStars || meetsCat) {
                progressDao.insertSticker(UnlockedStickerEntity(profileId = profileId, stickerId = sticker.id))
                newlyUnlocked.add(sticker)
            }
        }
        return newlyUnlocked
    }
}
