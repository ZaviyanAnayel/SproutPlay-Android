package com.zaviyanllc.sproutplay.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaviyanllc.sproutplay.audio.KidAudioManager
import com.zaviyanllc.sproutplay.data.local.AppSettingsEntity
import com.zaviyanllc.sproutplay.data.local.ChildProfileEntity
import com.zaviyanllc.sproutplay.data.local.GameProgressEntity
import com.zaviyanllc.sproutplay.data.local.SproutDatabase
import com.zaviyanllc.sproutplay.data.local.SproutRepository
import com.zaviyanllc.sproutplay.domain.model.AgeTier
import com.zaviyanllc.sproutplay.domain.model.StickerSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SproutUiState(
    val isLoading: Boolean = true,
    val profiles: List<ChildProfileEntity> = emptyList(),
    val activeProfile: ChildProfileEntity? = null,
    val progressMap: Map<String, GameProgressEntity> = emptyMap(),
    val unlockedStickerIds: Set<String> = emptySet(),
    val settings: AppSettingsEntity = AppSettingsEntity(),
    val reminderSnoozed: Boolean = false,
) {
    val activeTier: AgeTier
        get() = AgeTier.fromAge(activeProfile?.age ?: 6)

    val needsOnboarding: Boolean
        get() = !isLoading && activeProfile == null

    val minutesUsedToday: Int
        get() = settings.secondsUsedToday / 60

    val showScreenTimeReminder: Boolean
        get() = !reminderSnoozed &&
            settings.dailyTimeLimitMinutes > 0 &&
            settings.secondsUsedToday >= settings.dailyTimeLimitMinutes * 60
}

@OptIn(ExperimentalCoroutinesApi::class)
class SproutAppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SproutDatabase.getInstance(application)
    private val repository = SproutRepository(db.profileDao(), db.progressDao(), db.settingsDao())
    val audioManager = KidAudioManager(application)

    private val reminderSnoozed = MutableStateFlow(false)

    private val activeProfileFlow = combine(
        repository.allProfiles,
        repository.settings,
    ) { profiles, settings ->
        val active = profiles.firstOrNull { it.id == settings.activeProfileId }
            ?: profiles.firstOrNull()
        Triple(profiles, settings, active)
    }

    private val progressFlow = activeProfileFlow.flatMapLatest { (_, _, active) ->
        if (active == null) flowOf(emptyList())
        else repository.observeProgress(active.id)
    }

    private val stickersFlow = activeProfileFlow.flatMapLatest { (_, _, active) ->
        if (active == null) flowOf(emptyList())
        else repository.observeStickers(active.id)
    }

    val uiState: StateFlow<SproutUiState> = combine(
        activeProfileFlow,
        progressFlow,
        stickersFlow,
        reminderSnoozed,
    ) { (profiles, settings, active), progress, stickers, snoozed ->
        audioManager.soundEnabled = settings.soundEnabled
        audioManager.voiceEnabled = settings.voicePromptsEnabled
        audioManager.musicCuesEnabled = settings.musicCuesEnabled

        SproutUiState(
            isLoading = false,
            profiles = profiles,
            activeProfile = active,
            progressMap = progress.associateBy { it.gameId },
            unlockedStickerIds = stickers.map { it.stickerId }.toSet(),
            settings = settings,
            reminderSnoozed = snoozed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SproutUiState(isLoading = true),
    )

    init {
        // Gentle local session time tracker for Parent Controls screen-time reminder
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                if (uiState.value.activeProfile != null) {
                    repository.addPlaySeconds(15)
                }
            }
        }
    }

    fun completeOnboarding(name: String, age: Int, avatarId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val cleanName = name.trim().ifEmpty { "Explorer" }
            repository.createAndActivateProfile(cleanName, age, avatarId)
            audioManager.playCelebration()
            audioManager.speak("Hi $cleanName! Let's play and learn!")
            onDone()
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.switchActiveProfile(profileId)
            reminderSnoozed.value = false
        }
    }

    fun updateProfile(profile: ChildProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
        }
    }

    fun recordGameResult(
        gameId: String,
        starsEarned: Int,
        score: Int,
        tier: AgeTier,
        onUnlockedStickers: (List<StickerSpec>) -> Unit,
    ) {
        val profile = uiState.value.activeProfile ?: return
        viewModelScope.launch {
            val newlyUnlocked = repository.recordGameResult(
                profileId = profile.id,
                gameId = gameId,
                starsEarnedThisRun = starsEarned,
                score = score,
                tierIndex = tier.levelIndex,
            )
            onUnlockedStickers(newlyUnlocked)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = enabled) }
        }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(voicePromptsEnabled = enabled) }
        }
    }

    fun setMusicCuesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(musicCuesEnabled = enabled) }
        }
    }

    fun setDailyTimeLimit(minutes: Int) {
        viewModelScope.launch {
            reminderSnoozed.value = false
            repository.updateSettings { it.copy(dailyTimeLimitMinutes = minutes) }
        }
    }

    fun snoozeTimeReminder() {
        reminderSnoozed.value = true
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
