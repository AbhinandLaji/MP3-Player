package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.ui.navigation.NowPlayingRoute
import com.example.smartshuffle.ui.theme.CutCornerShape6
import com.example.smartshuffle.ui.components.EqBars
import java.io.File
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel, navController: NavController) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            currentSong?.let { song ->
                NowPlayingBar(
                    song = song,
                    isPlaying = isPlaying,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onClick = { navController.navigate(NowPlayingRoute) }
                )
            }
        }
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading or empty library...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding
            ) {
                items(songs, key = { it.id }) { song ->
                    val isCurrentlyPlaying = song.id == currentSong?.id
                    SongRow(
                        song = song, 
                        isCurrentlyPlaying = isCurrentlyPlaying,
                        onClick = { viewModel.playSong(song) },
                        onQueueNext = { viewModel.queueSongNext(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SongRow(
    song: Song, 
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
    onQueueNext: (Song) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = CutCornerShape6,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    text = { Text("Play Next (Queue)") },
                    onClick = {
                        onQueueNext(song)
                        showMenu = false
                    }
                )
            }
        }
    }
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
