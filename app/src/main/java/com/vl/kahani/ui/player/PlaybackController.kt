package com.vl.kahani.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.*
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.vl.kahani.data.Chapter
import com.vl.kahani.data.Series
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Playback state for the audio player and the mini-player.
 *
 * Supports both simulated playback (for demo) and real audio playback via MediaPlayer.
 * When context is provided, uses real AudioPlayer for actual audio file playback.
 * Falls back to simulated clock if no audio URL is available.
 */
class PlaybackController private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: PlaybackController? = null

        fun getInstance(context: Context): PlaybackController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlaybackController(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        fun getExistingInstance(): PlaybackController? = INSTANCE
    }

    private val audioPlayer = AudioPlayer(context)
    private val mediaSessionManager = MediaSessionManager(context, this)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    init {
        mediaSessionManager.initialize()
    }
    
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
    var audioUrl by mutableStateOf<String?>(null)
        private set
    var realDurationSeconds by mutableFloatStateOf(0f)
        private set

    /** null = off, 0 = stop at the end of this chapter, otherwise minutes remaining. */
    var sleepTimerMinutes by mutableStateOf<Int?>(null)
        private set
    var sleepStopAtChapterEnd by mutableStateOf(false)
        private set
    var sleepSecondsRemaining by mutableFloatStateOf(0f)
        private set

    val isActive: Boolean get() = chapter != null

    val durationSeconds: Float get() = if (realDurationSeconds > 0f) realDurationSeconds else (chapter?.durationSeconds ?: 300).toFloat()

    val fraction: Float get() = (positionSeconds / durationSeconds).coerceIn(0f, 1f)

    fun start(series: Series, chapter: Chapter, atFraction: Float = 0f, audioUrl: String? = null) {
        this.series = series
        this.chapter = chapter
        this.audioUrl = audioUrl
        this.realDurationSeconds = 0f // Reset for new audio
        positionSeconds = chapter.durationSeconds * atFraction.coerceIn(0f, 1f)
        
        // Load audio if available
        if (audioUrl != null) {
            audioPlayer.loadAudio(audioUrl)
            audioPlayer.seekTo(positionSeconds)
            audioPlayer.onPrepared = { dur ->
                realDurationSeconds = dur
                mediaSessionManager.updateMediaMetadata(
                    title = chapter.title,
                    artist = series.title,
                    durationMs = (dur * 1000).toLong(),
                    chapterNumber = chapter.chapterNumber
                )
            }
            audioPlayer.onPositionUpdate = { pos ->
                positionSeconds = pos
                mediaSessionManager.updatePlaybackState(isPlaying, (pos * 1000).toLong(), (durationSeconds * 1000).toLong())
            }
            audioPlayer.onPlaybackComplete = {
                isPlaying = false
                mediaSessionManager.updatePlaybackState(false, (durationSeconds * 1000).toLong(), (durationSeconds * 1000).toLong())
            }
            audioPlayer.play()
        }
        isPlaying = true
        expanded = true
        
        // Update media session and show notification
        mediaSessionManager.updateMediaMetadata(
            title = chapter.title,
            artist = series.title,
            durationMs = (durationSeconds * 1000).toLong(),
            chapterNumber = chapter.chapterNumber,
            artwork = null // Will be updated asynchronously
        )
        
        scope.launch {
            val bitmap = loadArtwork(context, series.coverUrl) ?: createPlaceholderArtwork(series.title)
            
            mediaSessionManager.updateMediaMetadata(
                title = chapter.title,
                artist = series.title,
                durationMs = (durationSeconds * 1000).toLong(),
                chapterNumber = chapter.chapterNumber,
                artwork = bitmap
            )
            
            mediaSessionManager.showPlaybackNotification(
                seriesTitle = series.title,
                chapterTitle = chapter.title,
                isPlaying = true,
                chapterNumber = chapter.chapterNumber,
                artwork = bitmap
            )

            // Increment play stat
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("series").document(series.id)
                .update("playCount", com.google.firebase.firestore.FieldValue.increment(1))
        }
    }

    private suspend fun loadArtwork(context: Context, url: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (url.isNullOrBlank()) return@withContext null
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Required for notification bitmaps
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else null
        } catch (e: Exception) { null }
    }

    private fun createPlaceholderArtwork(title: String): Bitmap {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = 0xFF800000.toInt() // Maroon
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, 200f, 200f, paint)
        paint.color = 0xFFFF1744.toInt() // Crimson (Replaces Saffron)
        paint.textSize = 80f
        paint.textAlign = Paint.Align.CENTER
        val initial = title.firstOrNull()?.toString() ?: "K"
        canvas.drawText(initial, 100f, 130f, paint)
        return bitmap
    }

    fun togglePlay() {
        if (chapter != null) {
            if (isPlaying) pause() else play()
        }
    }

    fun play() {
        if (chapter != null && !isPlaying) {
            isPlaying = true
            if (audioUrl != null) {
                audioPlayer.play()
            }
            updateNotification()
            mediaSessionManager.updatePlaybackState(true, (positionSeconds * 1000).toLong(), (durationSeconds * 1000).toLong())
        }
    }

    fun pause() {
        if (chapter != null && isPlaying) {
            isPlaying = false
            if (audioUrl != null) {
                audioPlayer.pause()
            }
            updateNotification()
            mediaSessionManager.updatePlaybackState(false, (positionSeconds * 1000).toLong(), (durationSeconds * 1000).toLong())
        }
    }

    private fun updateNotification() {
        val s = series
        val c = chapter
        if (s != null && c != null) {
            scope.launch {
                val bitmap = loadArtwork(context, s.coverUrl) ?: createPlaceholderArtwork(s.title)
                mediaSessionManager.showPlaybackNotification(
                    seriesTitle = s.title,
                    chapterTitle = c.title,
                    isPlaying = isPlaying,
                    chapterNumber = c.chapterNumber,
                    artwork = bitmap
                )
            }
        }
    }

    fun seekTo(seconds: Float) {
        positionSeconds = seconds.coerceIn(0f, durationSeconds)
        audioPlayer.seekTo(positionSeconds)
        mediaSessionManager.updatePlaybackState(isPlaying, (positionSeconds * 1000).toLong(), (durationSeconds * 1000).toLong())
    }

    fun skip(seconds: Int) = seekTo(positionSeconds + seconds)

    fun updateSpeed(value: Float) {
        speed = value
        audioPlayer.setPlaybackSpeed(value)
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
        audioUrl = null
        audioPlayer.pause()
        audioPlayer.release()
        mediaSessionManager.cancelNotification()
        clearSleepTimer()
    }

    /** Advances the simulated clock. Returns true when the chapter just finished. 
     * Only used when audioPlayer is not available or not playing real audio.
     */
    fun tick(deltaSeconds: Float): Boolean {
        // If we have a real audio player, position is updated via onPositionUpdate callback
        if (audioUrl != null) {
            return false  // Position updates are handled by audioPlayer
        }
        
        // Fallback to simulated clock for demo mode
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
fun rememberPlaybackController(): PlaybackController {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember { PlaybackController.getInstance(context) }
}

val LocalPlayback = staticCompositionLocalOf<PlaybackController> { error("PlaybackController not provided") }

fun formatClock(seconds: Float): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
