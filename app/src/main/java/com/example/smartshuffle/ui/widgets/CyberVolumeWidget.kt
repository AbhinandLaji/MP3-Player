package com.example.smartshuffle.ui.widgets

import android.content.Context
import android.media.AudioManager
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius

import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

val VolumePercentKey = intPreferencesKey("cyber_volume_percent")

class CyberVolumeWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        provideContent {
            val prefs = currentState<Preferences>()
            val volumePercentageFloat = prefs[VolumePercentKey]?.toFloat() ?: run {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (maxVolume > 0) (currentVolume.toFloat() / maxVolume.toFloat()) * 100 else 0f
            }
            val volumePercentage = volumePercentageFloat

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0E14))
                        .padding(8.dp)
                        .cornerRadius(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // Volume Up
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(Color(0xFF1E2638))
                            .cornerRadius(8.dp)
                            .clickable(actionRunCallback<VolumeUpActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00F0FF)), fontWeight = FontWeight.Bold))
                    }

                    // Segments
                    Column(
                        modifier = GlanceModifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (i in 4 downTo 0) {
                            val threshold = i * 20
                            val isActive = volumePercentage > threshold
                            val color = if (isActive) Color(0xFFFF003C) else Color(0xFF00F0FF).copy(alpha = 0.2f)
                            
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(color)
                                    .cornerRadius(2.dp)
                            ) {}
                            if (i > 0) Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }

                    // Percentage
                    Text(
                        text = "${volumePercentage.toInt()}%",
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(if (volumePercentage == 0f) Color(0xFFFF003C) else Color.White),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(bottom = 8.dp)
                    )

                    // Volume Down
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(Color(0xFF1E2638))
                            .cornerRadius(8.dp)
                            .clickable(actionRunCallback<VolumeDownActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00F0FF)), fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Mute
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(Color(0xFF1E2638))
                            .cornerRadius(8.dp)
                            .clickable(actionRunCallback<VolumeMuteActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("M", style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(if (volumePercentage == 0f) Color(0xFFFF003C) else Color(0xFF00F0FF)), 
                            fontWeight = FontWeight.Bold
                        ))
                    }
                }
            }
        }
    }
}
