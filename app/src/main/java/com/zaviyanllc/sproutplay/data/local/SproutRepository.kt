package com.zaviyanllc.sproutplay.data.local

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

    /**
     * Record a level result. Stars and score are best-only: a replay can only
     * improve them. [score] is the coins earned on this run.
     */
    suspend fun recordGameResult(
        profileId: Long,
        gameId: String,
        starsEarnedThisRun: Int,
        score: Int,
    ) {
        val existing = progressDao.getProgress(profileId, gameId)
        val previousStars = existing?.starsEarned ?: 0
        val bestStars = maxOf(previousStars, starsEarnedThisRun.coerceIn(0, 3))
        val bestScore = maxOf(existing?.highScore ?: 0, score)

        progressDao.upsertProgress(
            GameProgressEntity(
                profileId = profileId,
                gameId = gameId,
                starsEarned = bestStars,
                highScore = bestScore,
                timesPlayed = (existing?.timesPlayed ?: 0) + 1,
                lastTierIndex = 0,
                lastPlayedAt = System.currentTimeMillis(),
            ),
        )

        val profile = profileDao.getProfileById(profileId) ?: return
        val today = System.currentTimeMillis() / 86_400_000L
        val nextStreak = when {
            profile.lastPlayedEpochDay == today -> profile.streakDays
            profile.lastPlayedEpochDay == today - 1L -> profile.streakDays + 1
            else -> 1
        }
        profileDao.updateProfile(
            profile.copy(
                totalStars = profile.totalStars + (bestStars - previousStars).coerceAtLeast(0),
                streakDays = nextStreak,
                lastPlayedEpochDay = today,
            ),
        )
    }
}
