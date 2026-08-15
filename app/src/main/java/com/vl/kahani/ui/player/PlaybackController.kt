package com.vl.kahani.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.vl.kahani.data.Chapter
import com.vl.kahani.data.Series

/**
 * Playback state for the audio player and the mini-player.
 *
 * There are no audio files yet, so the position is advanced by a clock in the app shell rather than
 * by a media player. Everything the UI touches — scrubber, speed, sleep timer, mini-player — is the
 * real control surface, so wiring a real player later replaces this class and nothing else.
 */
class PlaybackController {
    var series by mutableStateOf<Series?>(null)
        private set
    var chapter by mutableStateOf<Chapter?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionSeconds by mutableFloatStateOf(0f)
        private set
    var speed by mutableFloatStateOf(1f)
        private set
    var expanded by mutableStateOf(false)

    /** null = off, 0 = stop at the end of this chapter, otherwise minutes remaining. */
    var sleepTimerMinutes by mutableStateOf<Int?>(null)
        private set
    var sleepStopAtChapterEnd by mutableStateOf(false)
        private set
    var sleepSecondsRemaining by mutableFloatStateOf(0f)
        private set

    val isActive: Boolean get() = chapter != null

    val durationSeconds: Float get() = (chapter?.durationSeconds ?: 1).toFloat()

    val fraction: Float get() = (positionSeconds / durationSeconds).coerceIn(0f, 1f)

    fun start(series: Series, chapter: Chapter, atFraction: Float = 0f) {
        this.series = series
        this.chapter = chapter
        positionSeconds = chapter.durationSeconds * atFraction.coerceIn(0f, 1f)
        isPlaying = true
        expanded = true
    }

    fun togglePlay() {
        if (chapter != null) isPlaying = !isPlaying
    }

    fun seekTo(seconds: Float) {
        positionSeconds = seconds.coerceIn(0f, durationSeconds)
    }

    fun skip(seconds: Int) = seekTo(positionSeconds + seconds)

    fun updateSpeed(value: Float) {
        speed = value
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerMinutes = minutes
        sleepStopAtChapterEnd = false
        sleepSecondsRemaining = (minutes ?: 0) * 60f
    }

    fun setSleepAtChapterEnd() {
        sleepTimerMinutes = null
        sleepStopAtChapterEnd = true
        sleepSecondsRemaining = 0f
    }

    fun clearSleepTimer() {
        sleepTimerMinutes = null
        sleepStopAtChapterEnd = false
        sleepSecondsRemaining = 0f
    }

    fun close() {
        isPlaying = false
        expanded = false
        series = null
        chapter = null
        positionSeconds = 0f
        clearSleepTimer()
    }

    /** Advances the simulated clock. Returns true when the chapter just finished. */
    fun tick(deltaSeconds: Float): Boolean {
        if (!isPlaying || chapter == null) return false
        positionSeconds += deltaSeconds * speed
        if (sleepTimerMinutes != null) {
            sleepSecondsRemaining -= deltaSeconds
            if (sleepSecondsRemaining <= 0f) {
                isPlaying = false
                clearSleepTimer()
            }
        }
        if (positionSeconds >= durationSeconds) {
            positionSeconds = durationSeconds
            isPlaying = false
            if (sleepStopAtChapterEnd) clearSleepTimer()
            return true
        }
        return false
    }
}

@Composable
fun rememberPlaybackController(): PlaybackController = remember { PlaybackController() }

val LocalPlayback = staticCompositionLocalOf<PlaybackController> { error("PlaybackController not provided") }

fun formatClock(seconds: Float): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
