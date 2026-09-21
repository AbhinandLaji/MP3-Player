package com.example.smartshuffle.ui.widgets.receivers

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.example.smartshuffle.ui.widgets.CyberVolumeWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import android.media.AudioManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.smartshuffle.ui.widgets.VolumePercentKey

class CyberVolumeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CyberVolumeWidget()
    
    private val coroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            coroutineScope.launch {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val percent = if (maxVolume > 0) ((currentVolume.toFloat() / maxVolume.toFloat()) * 100).toInt() else 0

                GlanceAppWidgetManager(context).getGlanceIds(CyberVolumeWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[VolumePercentKey] = percent
                    }
                }
                glanceAppWidget.updateAll(context)
            }
        }
    }
}
