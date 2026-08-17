package com.vl.kahani.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.PlaybackState
import com.vl.kahani.MainActivity

/**
 * Manages MediaSession and notification for audio playback.
 * Displays playback controls on lock screen and notification center.
 */
class MediaSessionManager(
    private val context: Context,
    private val playbackController: PlaybackController
) {
    private var mediaSession: MediaSession? = null
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "kahani_playback"
        private const val NOTIFICATION_ID = 1
    }

    fun initialize() {
        if (mediaSession != null) return
        
        mediaSession = MediaSession(context, "Kahani").apply {
            isActive = true
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    playbackController.play()
                }

                override fun onPause() {
                    playbackController.pause()
                }

                override fun onSkipToNext() {
                    playbackController.skip(30)
                }

                override fun onSkipToPrevious() {
                    playbackController.skip(-10)
                }
                
                override fun onSeekTo(pos: Long) {
                    playbackController.seekTo(pos / 1000f)
                }
            })
        }

        createNotificationChannel()
    }

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long, durationMs: Long) {
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        
        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(state, positionMs, 1f)
                .setActions(
                    PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_SEEK_TO
                )
                .build()
        )
    }

    fun updateMediaMetadata(title: String, artist: String, durationMs: Long, chapterNumber: Int = 1, artwork: android.graphics.Bitmap? = null) {
        val builder = android.media.MediaMetadata.Builder()
            .putString(android.media.MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, artist)
            .putString(android.media.MediaMetadata.METADATA_KEY_ALBUM, "Chapter $chapterNumber")
            .putLong(android.media.MediaMetadata.METADATA_KEY_DURATION, durationMs)
        
        if (artwork != null) {
            builder.putBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART, artwork)
        }

        mediaSession?.setMetadata(builder.build())
    }

    fun showPlaybackNotification(
        seriesTitle: String,
        chapterTitle: String,
        isPlaying: Boolean,
        chapterNumber: Int,
        artwork: android.graphics.Bitmap? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseActionIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(context, playPauseActionIcon),
            if (isPlaying) "Pause" else "Play",
            getPlayPauseIntent()
        ).build()

        val skipNextAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_media_next),
            "Next",
            getSkipIntent()
        ).build()
        
        val skipPrevAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_media_previous),
            "Previous",
            getPrevIntent()
        ).build()

        val notificationBuilder = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(com.vl.kahani.R.mipmap.ic_launcher)
            .setContentTitle(chapterTitle)
            .setContentText(seriesTitle)
            .setSubText("Chapter $chapterNumber")
            .setContentIntent(pendingIntent)
            .setDeleteIntent(getPauseIntent())
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .addAction(skipPrevAction)
            .addAction(playPauseAction)
            .addAction(skipNextAction)
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(isPlaying)

        if (artwork != null) {
            notificationBuilder.setLargeIcon(artwork)
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun release() {
        mediaSession?.release()
        mediaSession = null
        cancelNotification()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Kahani Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Kahani audiobook playback controls"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun getPlayPauseIntent(): PendingIntent {
        val intent = Intent(context, PlaybackReceiver::class.java).apply {
            action = PlaybackReceiver.ACTION_PLAY_PAUSE
        }
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getPauseIntent(): PendingIntent {
        val intent = Intent(context, PlaybackReceiver::class.java).apply {
            action = PlaybackReceiver.ACTION_PAUSE
        }
        return PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getSkipIntent(): PendingIntent {
        val intent = Intent(context, PlaybackReceiver::class.java).apply {
            action = PlaybackReceiver.ACTION_SKIP
        }
        return PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun getPrevIntent(): PendingIntent {
        val intent = Intent(context, PlaybackReceiver::class.java).apply {
            action = PlaybackReceiver.ACTION_PREV
        }
        return PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
