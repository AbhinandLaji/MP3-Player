package com.example.smartshuffle.ui.widgets

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.smartshuffle.playback.PlaybackService

class PlayPauseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        sendMediaCommand(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }
}

class SkipNextActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        sendMediaCommand(context, KeyEvent.KEYCODE_MEDIA_NEXT)
    }
}

class SkipPrevActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        sendMediaCommand(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }
}

private fun sendMediaCommand(context: Context, keyCode: Int) {
    val downIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
        setClass(context, PlaybackService::class.java)
        putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    }
    val upIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
        setClass(context, PlaybackService::class.java)
        putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
    context.startService(downIntent)
    context.startService(upIntent)
}

class VolumeUpActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
        CyberVolumeWidget().updateAll(context)
    }
}

class VolumeDownActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
        CyberVolumeWidget().updateAll(context)
    }
}

class VolumeMuteActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        CyberVolumeWidget().updateAll(context)
    }
}
