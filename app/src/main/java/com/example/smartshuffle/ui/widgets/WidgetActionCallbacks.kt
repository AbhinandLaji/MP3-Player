package com.example.smartshuffle.ui.widgets

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.state.updateAppWidgetState
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
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val target = (current + 1).coerceAtMost(max)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        
        val percent = if (max > 0) ((target.toFloat() / max.toFloat()) * 100).toInt() else 0
        updateWidgetVolume(context, glanceId, percent)
    }
}

class VolumeDownActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val target = (current - 1).coerceAtLeast(0)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)

        val percent = if (max > 0) ((target.toFloat() / max.toFloat()) * 100).toInt() else 0
        updateWidgetVolume(context, glanceId, percent)
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
        updateWidgetVolume(context, glanceId, 0)
    }
}

private suspend fun updateWidgetVolume(context: Context, glanceId: GlanceId, percent: Int) {
    updateAppWidgetState(context, glanceId) { prefs ->
        prefs[VolumePercentKey] = percent
    }
    CyberVolumeWidget().update(context, glanceId)
}
