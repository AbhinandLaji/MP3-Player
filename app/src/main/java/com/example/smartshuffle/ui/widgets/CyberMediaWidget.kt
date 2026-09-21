package com.example.smartshuffle.ui.widgets

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.ContentScale
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

class CyberMediaWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val titleKey = stringPreferencesKey("track_title")
        val artistKey = stringPreferencesKey("artist_name")
        val isPlayingKey = booleanPreferencesKey("is_playing")
        val artworkUriKey = stringPreferencesKey("artwork_uri")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val title = prefs[titleKey] ?: "No Song Playing"
        val artist = prefs[artistKey] ?: "Smart Shuffle"
        val isPlaying = prefs[isPlayingKey] ?: false
        val artworkUri = prefs[artworkUriKey]

        var artworkBitmap: Bitmap? = null
        if (!artworkUri.isNullOrEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(artworkUri)
                .size(150, 150)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                artworkBitmap = result.drawable.toBitmap()
            }
        }

        provideContent {
            GlanceTheme {
                // Outer Container: Deep dark background (#080C10)
                // Outline stroke / border tinted NeonBlue (#00E5FF) is simulated with a nested box.
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF00E5FF))
                        .cornerRadius(12.dp)
                        .padding(1.dp) // 1dp border
                ) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(Color(0xFF080C10))
                            .cornerRadius(11.dp)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album Art Artwork Frame (Left): Square container 60.dp x 60.dp
                        if (artworkBitmap != null) {
                            Image(
                                provider = ImageProvider(artworkBitmap),
                                contentDescription = "Album Art",
                                modifier = GlanceModifier
                                    .size(60.dp)
                                    .cornerRadius(8.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = GlanceModifier
                                    .size(60.dp)
                                    .background(Color(0xFF080C10))
                                    .cornerRadius(8.dp)
                                    .padding(1.dp), // For NeonRed frame
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFF003C))
                                        .cornerRadius(7.dp)
                                        .padding(1.dp)
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .background(Color(0xFF080C10))
                                            .cornerRadius(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "♪",
                                            style = TextStyle(
                                                color = androidx.glance.unit.ColorProvider(Color(0xFFFF003C)),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Typography & Info (Center)
                        Column(
                            modifier = GlanceModifier.defaultWeight()
                        ) {
                            // Status text glowing pip
                            Text(
                                text = if (isPlaying) "[PLAYING]" else "[PAUSED]",
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(if (isPlaying) Color(0xFFFF003C) else Color.Gray),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            
                            // Track title: Bold white, 14.sp, single line
                            Text(
                                text = title,
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(Color.White),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1
                            )
                            
                            // Artist name: NeonBlue accent, 12.sp, single line
                            Text(
                                text = artist,
                                style = TextStyle(
                                    color = androidx.glance.unit.ColorProvider(Color(0xFF00E5FF)),
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // HUD Control Cluster (Right)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Skip Prev: chamfered dark pad
                            Box(
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .background(Color(0xFF1E2638))
                                    .cornerRadius(6.dp)
                                    .clickable(actionRunCallback<SkipPrevActionCallback>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("|◀", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00E5FF)), fontWeight = FontWeight.Bold))
                            }
                            
                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Play/Pause: Glowing NeonRed center square
                            Box(
                                modifier = GlanceModifier
                                    .size(40.dp)
                                    .background(Color(0xFF1E2638))
                                    .cornerRadius(8.dp)
                                    .padding(1.dp) // Border space
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFF003C))
                                        .cornerRadius(7.dp)
                                        .padding(1.dp)
                                ) {
                                    Box(
                                        modifier = GlanceModifier
                                            .fillMaxSize()
                                            .background(Color(0xFF080C10))
                                            .cornerRadius(6.dp)
                                            .clickable(actionRunCallback<PlayPauseActionCallback>()),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val playIcon = if (isPlaying) "||" else "►"
                                        Text(playIcon, style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFFFF003C)), fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Skip Next
                            Box(
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .background(Color(0xFF1E2638))
                                    .cornerRadius(6.dp)
                                    .clickable(actionRunCallback<SkipNextActionCallback>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶|", style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFF00E5FF)), fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}
