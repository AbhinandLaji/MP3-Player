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
import androidx.compose.ui.layout.layout
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
    val systemVolume by viewModel.systemVolume.collectAsState()
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
            currentVolume = systemVolume,
            onPausePlayClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.skipNext() },
            onPrevClick = { viewModel.skipPrevious() },
            onShuffleToggle = { viewModel.toggleShuffle() },
            onQueueClick = { showQueue = true },
            onSleepTimerClick = { showSleepTimer = true },
            isSleepTimerActive = sleepTimerTargetMillis != null,
            onSeek = { viewModel.seekTo(it) },
            onVolumeChange = { viewModel.setSystemVolume(it) }
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

    val neonGlowPaint = remember {
        Paint().apply {
            color = androidx.compose.ui.graphics.Color(0xFFFF2A4D).copy(alpha = 0.6f) // NeonRed
            asFrameworkPaint().maskFilter = BlurMaskFilter(64f, BlurMaskFilter.Blur.NORMAL)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAIN CONTENT COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 48.dp) // Leave room for the volume bar on the right!
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
                        drawIntoCanvas { canvas ->
                            canvas.drawRoundRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                radiusX = 16f, // Match your surface corner radius
                                radiusY = 16f,
                                paint = neonGlowPaint
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

            PlaybackSeekBar(
                currentPosition = currentPositionMs,
                duration = totalDurationMs,
                onSeekFinished = onSeek,
                modifier = Modifier.fillMaxWidth()
            )

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

        VerticalVolumeSlider(
            systemVolume = currentVolume,
            onVolumeChanged = onVolumeChange,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .fillMaxHeight(0.30f)
        )
    }
}

@Composable
private fun PlaybackSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeekFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val progress = if (isDragging) {
        dragProgress
    } else if (duration > 0) {
        (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
    } else {
        0f
    }

    val activeSeekColor = if (isDragging) NeonRed else NeonBlue

    Column(modifier = modifier) {
        Slider(
            value = progress,
            onValueChange = {
                isDragging = true
                dragProgress = it
            },
            onValueChangeFinished = {
                onSeekFinished((dragProgress * duration).toLong())
                isDragging = false
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
                (dragProgress * duration).toLong()
            } else {
                currentPosition
            }
            Text(
                text = formatTime(displayPositionMs),
                fontFamily = ChakraPetch,
                fontSize = 12.sp,
                color = if (isDragging) NeonRed else TextSecondary
            )
            Text(
                text = formatTime(duration),
                fontFamily = ChakraPetch,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun VerticalVolumeSlider(
    systemVolume: Float,
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var localVolume by remember { mutableFloatStateOf(0f) }

    val volume = if (isDragging) localVolume else systemVolume
    val activeVolumeColor = if (isDragging) NeonRed else NeonBlue

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Max Volume",
            tint = NeonBlue.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = volume,
                onValueChange = {
                    isDragging = true
                    localVolume = it
                },
                onValueChangeFinished = {
                    onVolumeChanged(localVolume)
                    isDragging = false
                },
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 270f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minWidth = constraints.minHeight,
                                maxWidth = constraints.maxHeight,
                                minHeight = constraints.minWidth,
                                maxHeight = constraints.maxWidth
                            )
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                -(placeable.width - placeable.height) / 2,
                                -(placeable.height - placeable.width) / 2
                            )
                        }
                    },
                colors = SliderDefaults.colors(
                    thumbColor = activeVolumeColor,
                    activeTrackColor = activeVolumeColor,
                    inactiveTrackColor = NeonBlue.copy(alpha = 0.25f)
                )
            )
        }

        Icon(
            imageVector = Icons.Default.VolumeMute,
            contentDescription = "Min Volume",
            tint = NeonBlue.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}
