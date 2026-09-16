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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.components.GlitchText
import com.example.smartshuffle.ui.components.NeonIconButton
import com.example.smartshuffle.ui.components.QueueBottomSheet
import com.example.smartshuffle.ui.theme.CutCornerShape6
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import android.graphics.BlurMaskFilter

import com.example.smartshuffle.ui.theme.*

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes % 60, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

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
    val sleepTimerTargetMillis by viewModel.sleepTimerTargetMillis.collectAsState()
    
    var showQueue by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }

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
            onQueueClick = { showQueue = true },
            onSleepTimerClick = { showSleepTimer = true },
            isSleepTimerActive = sleepTimerTargetMillis != null,
            onSeek = { viewModel.seekTo(it) },
            onVolumeChange = { viewModel.setVolume(it) }
        )
        
        if (showQueue) {
            QueueBottomSheet(
                viewModel = viewModel,
                onDismiss = { showQueue = false }
            )
        }
        
        if (showSleepTimer) {
            com.example.smartshuffle.ui.components.SleepTimerBottomSheet(
                viewModel = viewModel,
                onDismiss = { showSleepTimer = false }
            )
        }
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
    onQueueClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    isSleepTimerActive: Boolean,
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
                // 1. Change the border to the secondary (Red) color
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    // 2. Add the red neon backlight glow
                    .drawBehind {
                        val shadowRadius = 64f // Controls how far the gradient glow spreads
                        
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                color = androidx.compose.ui.graphics.Color(0xFFFF2A4D).copy(alpha = 0.6f) // NeonRed
                                asFrameworkPaint().maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
                            }
                            
                            canvas.drawRoundRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                radiusX = 16f, // Match your surface corner radius
                                radiusY = 16f,
                                paint = paint
                            )
                        }
                    }
                    .padding(4.dp) // Slight padding so the glow bleeds out past the border
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Shuffle Toggle Button
                IconButton(
                    onClick = onShuffleToggle
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) NeonRed else NeonBlue.copy(alpha = 0.6f)
                    )
                }

                // CENTER: Sleep Timer Toggle Button
                IconButton(
                    onClick = onSleepTimerClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (isSleepTimerActive) NeonRed else NeonBlue.copy(alpha = 0.6f)
                    )
                }

                // RIGHT: Queue Sheet Toggle Button
                IconButton(
                    onClick = onQueueClick
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = NeonBlue
                    )
                }
            }

            // FIXED SEEK BAR
            val activeSeekColor = if (isDragging) NeonRed else NeonBlue
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
                    thumbColor = activeSeekColor,
                    activeTrackColor = activeSeekColor,
                    inactiveTrackColor = NeonSurfaceHi
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val displayPositionMs = if (isDragging) {
                    (dragProgress * totalDurationMs).toLong()
                } else {
                    currentPositionMs
                }
                Text(
                    text = formatTime(displayPositionMs),
                    fontFamily = ChakraPetch,
                    fontSize = 12.sp,
                    color = if (isDragging) NeonRed else TextSecondary
                )
                Text(
                    text = formatTime(totalDurationMs),
                    fontFamily = ChakraPetch,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CYBERPUNK CONTROLS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonIconButton(icon = Icons.Default.SkipPrevious, contentDescription = "Prev", onClick = onPrevClick)

                // Play/Pause gets a larger visual emphasis
                NeonIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(80.dp),
                    color = if (!isPlaying) NeonRed else NeonBlue,
                    onClick = onPausePlayClick
                )

                NeonIconButton(icon = Icons.Default.SkipNext, contentDescription = "Next", onClick = onNextClick)
            }
            
            Spacer(modifier = Modifier.weight(1.2f))
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

            var isVolumeDragging by remember { mutableStateOf(false) }
            val activeVolumeColor = if (isVolumeDragging) NeonRed else NeonBlue

            // Rotate a standard horizontal slider 90 degrees to make it vertical
            Box(modifier = Modifier.weight(1f).width(48.dp), contentAlignment = Alignment.Center) {
                Slider(
                    value = currentVolume,
                    onValueChange = {
                        isVolumeDragging = true
                        onVolumeChange(it)
                    },
                    onValueChangeFinished = {
                        isVolumeDragging = false
                    },
                    modifier = Modifier
                        .graphicsLayer {
                            rotationZ = -90f
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                        }
                        .requiredWidth(250.dp), // Height of the vertical bar
                    colors = SliderDefaults.colors(
                        thumbColor = activeVolumeColor,
                        activeTrackColor = activeVolumeColor.copy(alpha = 0.5f)
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
