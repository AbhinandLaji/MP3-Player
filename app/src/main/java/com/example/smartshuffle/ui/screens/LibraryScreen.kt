package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.theme.CutCornerShape6
import com.example.smartshuffle.ui.theme.NeonRed
import com.example.smartshuffle.ui.theme.NeonSurfaceHi
import com.example.smartshuffle.ui.theme.TextSecondary
import com.example.smartshuffle.ui.components.activeTrackCyberBorder
import com.example.smartshuffle.ui.components.cyberGridCardBorder
import androidx.compose.foundation.shape.CutCornerShape
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Queue
import com.example.smartshuffle.ui.components.EqBars
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.example.smartshuffle.ui.theme.NeonBlue

@Composable
fun LibraryScreen(viewModel: LibraryViewModel, navController: NavController, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()

    if (songs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading or empty library...")
        }
    } else {
        val gridState = rememberLazyGridState()
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 80.dp // To prevent overlap with hoisted NowPlayingBar
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs, key = { it.id }) { song ->
                val isCurrentlyPlaying = song.id == currentSong?.id
                SongGridItem(
                    song = song, 
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    onClick = { viewModel.playSong(song) }
                )
            }
        }
    }
}

@Composable
fun SongGridItem(
    song: Song, 
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Album Artwork Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CutCornerShape(12.dp))
                .cyberGridCardBorder(
                    primaryColor = if (isCurrentlyPlaying) NeonRed else NeonRed.copy(alpha = 0.5f),
                    cutSize = 12.dp,
                    strokeWidth = if (isCurrentlyPlaying) 2.dp else 1.2.dp
                )
        ) {
            coil.compose.AsyncImage(
                model = song.albumArtUri,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                // Use a standard painter or color fallback instead of complex layouts for error handling if possible.
                // In this case, removing the subcompose loading/error states significantly speeds up initial layout measurement.
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Metadata
        Text(
            text = song.title,
            color = if (isCurrentlyPlaying) NeonRed else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = song.artist,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NowPlayingBar(song: Song, isPlaying: Boolean, onPlayPauseToggle: () -> Unit, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

@Composable
fun SongRow(
    song: Song, 
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .then(
                if (isCurrentlyPlaying) {
                    Modifier.activeTrackCyberBorder(
                        borderColor = NeonRed,
                        backgroundColor = NeonRed.copy(alpha = 0.12f),
                        cutSize = 10.dp
                    )
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface, CutCornerShape6)
                }
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatDuration(song.duration),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        if (isCurrentlyPlaying) {
            EqBars(isPlaying = true, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Song Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("PLAY NEXT") },
                    onClick = {
                        onPlayNext(song)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.PlaylistPlay, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("ADD TO QUEUE") },
                    onClick = {
                        onAddToQueue(song)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Queue, contentDescription = null) }
                )
            }
        }
    }
    }
}

