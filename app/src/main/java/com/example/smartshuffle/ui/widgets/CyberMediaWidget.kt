package com.example.smartshuffle.ui.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class CyberMediaWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val titleKey = stringPreferencesKey("title")
        val artistKey = stringPreferencesKey("artist")
        val isPlayingKey = booleanPreferencesKey("is_playing")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val title = prefs[titleKey] ?: "No Song Playing"
            val artist = prefs[artistKey] ?: "Smart Shuffle"
            val isPlaying = prefs[isPlayingKey] ?: false

            GlanceTheme {
                Row(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0E14))
                        .padding(12.dp)
                        .cornerRadius(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art Placeholder
                    Box(
                        modifier = GlanceModifier
                            .size(64.dp)
                            .background(Color(0xFF1E2638))
                            .cornerRadius(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♪",
                            style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00F0FF)), fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    // Title & Artist
                    Column(
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = title,
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color.White),
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = artist,
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color.Gray)
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    // Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Skip Prev
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(Color(0xFF1E2638))
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<SkipPrevActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("|<", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00F0FF)), fontWeight = FontWeight.Bold))
                        }
                        
                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Play/Pause
                        Box(
                            modifier = GlanceModifier
                                .size(48.dp)
                                .background(Color(0xFF1E2638))
                                .cornerRadius(24.dp)
                                .clickable(actionRunCallback<PlayPauseActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            val playIcon = if (isPlaying) "||" else "►"
                            val iconColor = if (isPlaying) Color(0xFFFF003C) else Color(0xFF00F0FF)
                            Text(playIcon, style = TextStyle(color = androidx.glance.unit.ColorProvider(iconColor), fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Skip Next
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(Color(0xFF1E2638))
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<SkipNextActionCallback>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(">|", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00F0FF)), fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
