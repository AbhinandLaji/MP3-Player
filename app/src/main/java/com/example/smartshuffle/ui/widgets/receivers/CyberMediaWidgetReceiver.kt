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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey

class CyberMediaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CyberMediaWidget()
    
    private val coroutineScope = MainScope()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    companion object {
        fun updateWidgetState(
            context: Context,
            title: String,
            artist: String,
            isPlaying: Boolean,
            artworkUri: String? = null
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(CyberMediaWidget::class.java)
                glanceIds.forEach { id ->
                    updateAppWidgetState(context, id) { prefs ->
                        prefs[stringPreferencesKey("track_title")] = title
                        prefs[stringPreferencesKey("artist_name")] = artist
                        prefs[booleanPreferencesKey("is_playing")] = isPlaying
                        prefs[stringPreferencesKey("artwork_uri")] = artworkUri ?: ""
                    }
                    CyberMediaWidget().update(context, id)
                }
            }
        }
    }
}
