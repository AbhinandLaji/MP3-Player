package com.example.smartshuffle.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smartshuffle.SmartShuffleApplication
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.data.PlayHistoryDao
import com.example.smartshuffle.domain.FolderSummary
import com.example.smartshuffle.domain.SongRepository
import com.example.smartshuffle.playback.PlaybackController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val playHistoryDao: PlayHistoryDao,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val baseSongs = songRepository.getAllSongs()

    val songs: StateFlow<List<Song>> = kotlinx.coroutines.flow.combine(baseSongs, playHistoryDao.getPlayCounts()) { allSongs, playCounts ->
        val playCountMap = playCounts.associateBy({ it.songId }, { it.playCount })
        allSongs.sortedByDescending { playCountMap[it.id] ?: 0 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentSong: StateFlow<Song?> = playbackController.currentSong
    val isPlaying: StateFlow<Boolean> = playbackController.isPlaying
    val currentPosition: StateFlow<Long> = playbackController.currentPosition
    val duration: StateFlow<Long> = playbackController.duration
    val volume: StateFlow<Float> = playbackController.volume
    val isShuffleEnabled: StateFlow<Boolean> = playbackController.isShuffleEnabled
    
    val folders: StateFlow<List<FolderSummary>> = songRepository.getFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedFolderPath = MutableStateFlow<String?>(null)
    val selectedFolderSongs: StateFlow<List<Song>> = kotlinx.coroutines.flow.combine(baseSongs, _selectedFolderPath) { allSongs: List<Song>, path: String? ->
        if (path == null) emptyList()
        else allSongs.filter { java.io.File(it.filePath).parentFile?.absolutePath == path }.sortedBy { it.title }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedFolder(path: String) {
        _selectedFolderPath.value = path
    }

    init {
        viewModelScope.launch {
            songRepository.syncLibrary()
        }
    }

    fun playSong(song: Song, playlist: List<Song> = songs.value) {
        playbackController.playFromPlaylist(song, playlist)
    }

    fun queueSongNext(song: Song) {
        playbackController.queueSongNext(song)
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            playbackController.pause()
        } else {
            playbackController.resume()
        }
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun skipNext() {
        playbackController.skipNext()
    }

    fun skipPrevious() {
        playbackController.skipPrevious()
    }

    fun seekTo(position: Long) {
        playbackController.seekTo(position)
    }

    fun setVolume(volume: Float) {
        playbackController.setVolume(volume)
    }

    companion object {
        fun provideFactory(
            application: SmartShuffleApplication,
            playbackController: PlaybackController
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = application.container
                return LibraryViewModel(container.songRepository, container.playHistoryDao, playbackController) as T
            }
        }
    }
}
