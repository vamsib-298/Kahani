package com.vl.kahani.ui.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.roundToInt

/**
 * Real audio player implementation using Android MediaPlayer.
 * Handles actual audio playback instead of simulated clock.
 */
class AudioPlayer(context: Context) : AudioManager.OnAudioFocusChangeListener {
    companion object {
        private const val TAG = "AudioPlayer"
        private const val UPDATE_INTERVAL_MS = 200L
    }

    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updatePositionRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && mediaPlayer?.isPlaying == true) {
                val currentPos = mediaPlayer?.currentPosition?.div(1000f) ?: 0f
                onPositionUpdate(currentPos)
                handler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    var onPositionUpdate: (Float) -> Unit = {}
    var onPrepared: (Float) -> Unit = {}
    var onPlaybackComplete: () -> Unit = {}
    var onError: (String) -> Unit = {}

    private var _isPlaying: Boolean = false
    var isPlaying: Boolean
        get() = _isPlaying
        private set(value) { _isPlaying = value }

    private var _currentPosition: Float = 0f
    val currentPosition: Float get() = _currentPosition

    private var _duration: Float = 0f
    val duration: Float get() = _duration

    private var isPrepared = false
    private var pendingSeekSeconds: Float? = null

    fun loadAudio(audioUrl: String) {
        Log.d(TAG, "loadAudio called with URL: $audioUrl")
        isPrepared = false
        pendingSeekSeconds = null
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                Log.d(TAG, "Setting data source...")
                setDataSource(audioUrl)
                
                setOnPreparedListener {
                    isPrepared = true
                    _duration = (it.duration.div(1000f))
                    Log.d(TAG, "Audio prepared. Duration: $_duration seconds")
                    onPrepared(_duration)
                    
                    pendingSeekSeconds?.let { seekTo ->
                        seekTo(seekTo)
                        pendingSeekSeconds = null
                    }
                    
                    if (shouldPlayAfterPrepare) {
                        play()
                        shouldPlayAfterPrepare = false
                    }
                }
                
                setOnCompletionListener {
                    _isPlaying = false
                    Log.d(TAG, "Playback completed")
                    onPlaybackComplete()
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    onError("MediaPlayer error: $what, $extra")
                    false
                }
                
                Log.d(TAG, "Calling prepareAsync()...")
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load audio: ${e.message}", e)
            onError("Failed to load audio: ${e.message}")
        }
    }

    private var shouldPlayAfterPrepare = false

    fun play() {
        if (mediaPlayer == null) return
        
        if (!isPrepared) {
            shouldPlayAfterPrepare = true
            return
        }

        if (requestAudioFocus()) {
            try {
                if (!_isPlaying) {
                    mediaPlayer?.start()
                    _isPlaying = true
                    handler.post(updatePositionRunnable)
                    Log.d(TAG, "Playback started successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play: ${e.message}", e)
                onError("Failed to play: ${e.message}")
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            return audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> play()
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
        }
    }

    fun pause() {
        if (mediaPlayer == null) return
        Log.d(TAG, "pause() called. _isPlaying=$_isPlaying")
        try {
            if (_isPlaying) {
                Log.d(TAG, "Pausing playback...")
                mediaPlayer?.pause()
                _isPlaying = false
                handler.removeCallbacks(updatePositionRunnable)
                Log.d(TAG, "Playback paused successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause: ${e.message}", e)
            onError("Failed to pause: ${e.message}")
        }
    }

    fun seekTo(positionSeconds: Float) {
        if (mediaPlayer == null) return
        if (!isPrepared) {
            pendingSeekSeconds = positionSeconds
            return
        }
        try {
            val positionMs = (positionSeconds * 1000).roundToInt().coerceIn(0, (duration * 1000).toInt())
            mediaPlayer?.seekTo(positionMs)
            _currentPosition = positionSeconds
        } catch (e: Exception) {
            onError("Failed to seek: ${e.message}")
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        // Note: Speed control requires API 23+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = mediaPlayer!!.playbackParams.setSpeed(speed)
            } catch (e: Exception) {
                onError("Failed to set speed: ${e.message}")
            }
        }
    }

    fun release() {
        handler.removeCallbacks(updatePositionRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying = false
    }

}
