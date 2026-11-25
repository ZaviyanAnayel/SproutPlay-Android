package com.zaviyanllc.sproutplay.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.zaviyanllc.sproutplay.R

/**
 * Twin Trap audio: original synthesized sounds bundled in res/raw (made with
 * ffmpeg from pure sine/noise math — no copyrighted material).
 *
 * Short effects go through SoundPool; the 30s suspense loop plays through a
 * looping MediaPlayer during shuffles only. Respects the sound/music toggles.
 */
class TrapAudioManager(context: Context) {
    private val appContext = context.applicationContext

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var musicEnabled: Boolean = true

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val tickId = soundPool.load(appContext, R.raw.sfx_tick, 1)
    private val whooshId = soundPool.load(appContext, R.raw.sfx_whoosh, 1)
    private val popId = soundPool.load(appContext, R.raw.sfx_pop, 1)
    private val winId = soundPool.load(appContext, R.raw.sfx_win, 1)
    private val loseId = soundPool.load(appContext, R.raw.sfx_lose, 1)

    private var musicPlayer: MediaPlayer? = null

    private fun play(id: Int, volume: Float = 1f, rate: Float = 1f) {
        if (!soundEnabled) return
        try {
            soundPool.play(id, volume, volume, 1, 0, rate)
        } catch (_: Throwable) {
        }
    }

    fun playTap() = play(popId, volume = 0.35f, rate = 1.2f)

    fun playTick() = play(tickId)

    fun playWhoosh() = play(whooshId, volume = 0.7f)

    fun playPop() = play(popId)

    fun playWin() = play(winId)

    fun playLose() = play(loseId)

    /** Dull low blip for a wrong pick. */
    fun playWrong() = play(tickId, volume = 0.6f, rate = 0.55f)

    /** Start the suspense loop (during shuffles). Safe to call repeatedly. */
    fun startMusic() {
        if (!musicEnabled) return
        try {
            val player = musicPlayer ?: MediaPlayer.create(appContext, R.raw.music_loop)?.apply {
                isLooping = true
                setVolume(0.45f, 0.45f)
                musicPlayer = this
            } ?: return
            if (!player.isPlaying) player.start()
        } catch (_: Throwable) {
        }
    }

    fun stopMusic() {
        try {
            musicPlayer?.let { if (it.isPlaying) it.pause() }
        } catch (_: Throwable) {
        }
    }

    fun release() {
        try {
            musicPlayer?.release()
            musicPlayer = null
        } catch (_: Throwable) {
        }
        try {
            soundPool.release()
        } catch (_: Throwable) {
        }
    }
}
