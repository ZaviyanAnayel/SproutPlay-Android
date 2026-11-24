package com.zaviyanllc.sproutplay.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Kid-friendly audio & Text-to-Speech manager.
 * Synthesizes gentle marimba/xylophone chimes via AudioTrack (never harsh buzzers)
 * and speaks warm encouraging prompts via Android TextToSpeech.
 */
class KidAudioManager(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    var voiceEnabled: Boolean = true

    @Volatile
    var musicCuesEnabled: Boolean = true

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setPitch(1.08f)
                    tts?.setSpeechRate(0.92f)
                    ttsReady = true
                }
            }
        } catch (_: Throwable) {
            ttsReady = false
        }
    }

    fun speak(text: String, interrupt: Boolean = true) {
        if (!voiceEnabled || !ttsReady) return
        try {
            val mode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(text, mode, null, "sprout_${System.nanoTime()}")
        } catch (_: Throwable) {
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Throwable) {
        }
    }

    fun playTap() {
        if (!soundEnabled) return
        playTone(freqHz = 523.25, durationMs = 70, gain = 0.22)
    }

    fun playSuccess() {
        if (!soundEnabled) return
        scope.launch {
            playToneBlocking(523.25, 95, 0.25) // C5
            playToneBlocking(659.25, 95, 0.25) // E5
            playToneBlocking(783.99, 150, 0.28) // G5
        }
    }

    fun playCelebration() {
        if (!soundEnabled) return
        scope.launch {
            playToneBlocking(523.25, 100, 0.25)
            playToneBlocking(659.25, 100, 0.25)
            playToneBlocking(783.99, 100, 0.25)
            playToneBlocking(1046.50, 260, 0.30)
        }
    }

    fun playGentleTryAgain() {
        if (!soundEnabled) return
        // Soft warm descending wooden marimba tone — never a harsh buzzer!
        scope.launch {
            playToneBlocking(349.23, 110, 0.18) // F4
            playToneBlocking(293.66, 150, 0.16) // D4
        }
    }

    fun playMusicalNote(noteIndex: Int) {
        if (!soundEnabled && !musicCuesEnabled) return
        // C major scale C4..C5
        val freqs = doubleArrayOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25)
        val freq = freqs[noteIndex.coerceIn(0, freqs.lastIndex)]
        playTone(freqHz = freq, durationMs = 260, gain = 0.30)
    }

    private fun playTone(freqHz: Double, durationMs: Int, gain: Double) {
        scope.launch {
            playToneBlocking(freqHz, durationMs, gain)
        }
    }

    private fun playToneBlocking(freqHz: Double, durationMs: Int, gain: Double) {
        val sampleRate = 22050
        val count = (sampleRate * durationMs / 1000.0).toInt().coerceAtLeast(1)
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            // Smooth attack and exponential decay envelope for warm xylophone sound
            val attack = (i / (sampleRate * 0.012)).coerceAtMost(1.0)
            val decay = exp(-4.5 * i / count)
            val wave = sin(2.0 * PI * freqHz * t) + 0.22 * sin(4.0 * PI * freqHz * t)
            val sample = (wave * attack * decay * gain * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer[i] = sample.toShort()
        }
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 20L)
            track.stop()
            track.release()
        } catch (_: Throwable) {
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {
        }
    }
}
