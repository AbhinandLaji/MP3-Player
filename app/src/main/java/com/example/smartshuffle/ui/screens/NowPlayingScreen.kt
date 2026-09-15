package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.components.GlitchText
import com.example.smartshuffle.ui.components.NeonIconButton
import com.example.smartshuffle.ui.theme.CutCornerShape6
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberAlbumArt(uri: android.net.Uri): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val artBytes = retriever.embeddedPicture
                if (artBytes != null) {
                    bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size).asImageBitmap()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }
        }
    }
    return bitmap
}

@Composable
fun NowPlayingScreen(viewModel: LibraryViewModel, navController: NavController) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val volume by viewModel.volume.collectAsState()

    if (currentSong != null) {
        NowPlayingScreenContent(
            song = currentSong!!,
            isPlaying = isPlaying,
            isShuffleEnabled = isShuffleEnabled,
            currentPositionMs = currentPosition,
            totalDurationMs = duration,
            currentVolume = volume,
            onPausePlayClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.skipNext() },
            onPrevClick = { viewModel.skipPrevious() },
            onShuffleToggle = { viewModel.toggleShuffle() },
            onSeek = { viewModel.seekTo(it) },
            onVolumeChange = { viewModel.setVolume(it) }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }
}

@Composable
fun NowPlayingScreenContent(
    song: Song,
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    currentVolume: Float, // 0f to 1f
    onPausePlayClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    val albumArt = rememberAlbumArt(uri = android.net.Uri.parse(song.filePath))

    // --- THE SEEK BAR BUG FIX ---
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Use the drag state if the user is holding the slider, otherwise use ExoPlayer's state
    val displayProgress = if (isDragging) dragProgress else (currentPositionMs.toFloat() / totalDurationMs.coerceAtLeast(1).toFloat())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAIN CONTENT COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 64.dp) // Leave room for the volume bar on the right!
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            GlitchText(text = "NOW PLAYING")

            Spacer(modifier = Modifier.height(48.dp))

            // CYBERPUNK ALBUM ART
            Surface(
                shape = CutCornerShape6,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (albumArt != null) {
                    androidx.compose.foundation.Image(
                        bitmap = albumArt,
                        contentDescription = "Album Art",
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = song.title.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = song.artist.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // FIXED SEEK BAR
            Slider(
                value = displayProgress.coerceIn(0f, 1f),
                onValueChange = {
                    isDragging = true
                    dragProgress = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeek((dragProgress * totalDurationMs).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CYBERPUNK CONTROLS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (Red if active, Blue if inactive)
                IconButton(onClick = onShuffleToggle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                NeonIconButton(icon = Icons.Default.SkipPrevious, contentDescription = "Prev", onClick = onPrevClick)

                // Play/Pause gets a larger visual emphasis
                NeonIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(80.dp),
                    onClick = onPausePlayClick
                )

                NeonIconButton(icon = Icons.Default.SkipNext, contentDescription = "Next", onClick = onNextClick)
            }
            
            Spacer(modifier = Modifier.weight(0.4f))
        }

        // --- RIGHT-SIDE VERTICAL VOLUME BAR ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.6f) // Takes up 60% of screen height
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Max Volume",
                tint = MaterialTheme.colorScheme.primary
            )

            // Rotate a standard horizontal slider 90 degrees to make it vertical
            Box(modifier = Modifier.weight(1f).width(48.dp), contentAlignment = Alignment.Center) {
                Slider(
                    value = currentVolume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = -90f
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                        }
                        .requiredWidth(250.dp), // Height of the vertical bar
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.VolumeMute,
                contentDescription = "Mute",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
