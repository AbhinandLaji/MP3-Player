package com.example.smartshuffle.ui.widgets.receivers

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.smartshuffle.ui.widgets.CyberMediaWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CyberMediaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CyberMediaWidget()
    
    private val coroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE_MEDIA_WIDGET) {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "No Song Playing"
            val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Smart Shuffle"
            val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
            
            coroutineScope.launch {
                androidx.glance.appwidget.GlanceAppWidgetManager(context)
                    .getGlanceIds(CyberMediaWidget::class.java)
                    .forEach { glanceId ->
                        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[CyberMediaWidget.titleKey] = title
                                this[CyberMediaWidget.artistKey] = artist
                                this[CyberMediaWidget.isPlayingKey] = isPlaying
                            }
                        }
                        glanceAppWidget.update(context, glanceId)
                    }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_MEDIA_WIDGET = "com.example.smartshuffle.ACTION_UPDATE_MEDIA_WIDGET"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        
        fun updateWidgetState(context: Context, title: String, artist: String, isPlaying: Boolean) {
            val intent = Intent(context, CyberMediaWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_MEDIA_WIDGET
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            context.sendBroadcast(intent)
        }
    }
}
