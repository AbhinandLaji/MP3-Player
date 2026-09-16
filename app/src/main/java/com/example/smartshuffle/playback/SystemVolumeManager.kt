package com.example.smartshuffle.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SystemVolumeManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val maxVolume: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    val currentVolume: Int
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    fun setVolume(volumeLevel: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volumeLevel.coerceIn(0, maxVolume),
            0 // 0 flags = silent update (no default Android volume HUD overlay)
        )
    }

    // Flow emitting normalized volume (0.0f .. 1.0f) whenever volume changes
    fun observeVolume(): Flow<Float> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                    val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                    if (streamType == AudioManager.STREAM_MUSIC) {
                        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        trySend(if (max > 0) current.toFloat() / max else 0f)
                    }
                }
            }
        }

        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        context.registerReceiver(receiver, filter)

        // Initial emission
        val initialMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val initialCur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        trySend(if (initialMax > 0) initialCur.toFloat() / initialMax else 0f)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}
