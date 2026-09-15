package com.example.smartshuffle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.smartshuffle.ui.navigation.NowPlayingRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(viewModel: LibraryViewModel, navController: NavController) {
    val songs by viewModel.selectedFolderSongs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            items(songs, key = { it.id }) { song ->
                SongRow(
                    song = song, 
                    onClick = { viewModel.playSong(song, songs) },
                    onQueueNext = { viewModel.queueSongNext(it) }
                )
            }
        }
    }
}
