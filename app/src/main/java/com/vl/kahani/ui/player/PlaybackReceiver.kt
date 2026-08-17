package com.vl.kahani.ui.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PlaybackReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "PlaybackReceiver"
        const val ACTION_PLAY_PAUSE = "com.vl.kahani.PLAY_PAUSE"
        const val ACTION_PAUSE = "com.vl.kahani.PAUSE"
        const val ACTION_SKIP = "com.vl.kahani.SKIP"
        const val ACTION_PREV = "com.vl.kahani.PREV"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val controller = PlaybackController.getExistingInstance() ?: return
        Log.d(TAG, "onReceive: ${intent.action}")
        
        when (intent.action) {
            ACTION_PLAY_PAUSE -> controller.togglePlay()
            ACTION_PAUSE -> controller.pause()
            ACTION_SKIP -> controller.skip(30)
            ACTION_PREV -> controller.skip(-10)
        }
    }
}
