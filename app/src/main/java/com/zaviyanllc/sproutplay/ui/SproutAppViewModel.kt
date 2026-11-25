package com.zaviyanllc.sproutplay.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaviyanllc.sproutplay.audio.TrapAudioManager
import com.zaviyanllc.sproutplay.data.local.AppSettingsEntity
import com.zaviyanllc.sproutplay.data.local.GameProgressEntity
import com.zaviyanllc.sproutplay.data.local.SproutDatabase
import com.zaviyanllc.sproutplay.data.local.SproutRepository
import com.zaviyanllc.sproutplay.domain.model.TwinTrapLevels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Twin Trap state. No profiles UI, no onboarding, no TTS, no screen-time
 * limit — one silent "Player" profile is created on first launch and all
 * progress lives under it in Room. Coins are the sum of each level's best
 * coins (stored as GameProgressEntity.highScore); trophies are derived from
 * completed levels.
 */
data class TrapUiState(
    val isLoading: Boolean = true,
    val progressMap: Map<String, GameProgressEntity> = emptyMap(),
    val settings: AppSettingsEntity = AppSettingsEntity(),
) {
    val totalCoins: Int
        get() = progressMap.values.sumOf { it.highScore }

    val totalStars: Int
        get() = progressMap.values.sumOf { it.starsEarned }

    val levelsCompleted: Int
        get() = (1..10).count { starsFor(it) > 0 }

    fun starsFor(level: Int): Int =
        progressMap[TwinTrapLevels.gameId(level)]?.starsEarned ?: 0

    fun isUnlocked(level: Int): Boolean = level == 1 || starsFor(level - 1) > 0

    /** First level with no stars yet (or 1 if all are done). */
    val nextLevel: Int
        get() = (1..10).firstOrNull { starsFor(it) == 0 } ?: 1

    val trophies: List<String>
        get() = TwinTrapLevels.all.mapNotNull { level ->
            if (level.trophy != null && starsFor(level.number) > 0) level.trophy else null
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SproutAppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SproutDatabase.getInstance(application)
    private val repository = SproutRepository(db.profileDao(), db.progressDao(), db.settingsDao())
    val audioManager = TrapAudioManager(application)

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

    val uiState: StateFlow<TrapUiState> = combine(
        activeProfileFlow,
        progressFlow,
    ) { (_, settings, _), progress ->
        audioManager.soundEnabled = settings.soundEnabled
        audioManager.musicEnabled = settings.musicCuesEnabled
        TrapUiState(
            isLoading = false,
            progressMap = progress.associateBy { it.gameId },
            settings = settings,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrapUiState(isLoading = true),
    )

    init {
        // One silent player profile — no name/age onboarding.
        viewModelScope.launch {
            val profiles = repository.allProfiles.first()
            if (profiles.isEmpty()) {
                repository.createAndActivateProfile("Player", 6, "star")
            }
        }
    }

    fun recordLevelResult(level: Int, starsEarned: Int, coins: Int) {
        viewModelScope.launch {
            val activeId = repository.settings.first().activeProfileId
                ?: repository.allProfiles.first().firstOrNull()?.id
                ?: return@launch
            repository.recordGameResult(
                profileId = activeId,
                gameId = TwinTrapLevels.gameId(level),
                starsEarnedThisRun = starsEarned,
                score = coins,
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(soundEnabled = enabled) }
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { it.copy(musicCuesEnabled = enabled) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
