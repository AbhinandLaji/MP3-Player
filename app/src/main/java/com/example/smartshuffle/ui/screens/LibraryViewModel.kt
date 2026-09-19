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
import com.example.smartshuffle.playback.SystemVolumeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val playHistoryDao: PlayHistoryDao,
    private val playbackController: PlaybackController,
    private val volumeManager: SystemVolumeManager
) : ViewModel() {

    private val baseSongs = songRepository.getAllSongs()

    val songs: StateFlow<List<Song>> = kotlinx.coroutines.flow.combine(baseSongs, playHistoryDao.getPlayCounts()) { allSongs, playCounts ->
        val startTime = System.currentTimeMillis()
        val playCountMap = playCounts.associateBy({ it.songId }, { it.playCount })
        val result = allSongs.sortedByDescending { playCountMap[it.id] ?: 0 }
        val duration = System.currentTimeMillis() - startTime
        android.util.Log.d("PERF_AUDIT", "LibraryViewModel combined getAllSongs + getPlayCounts in ${duration}ms for ${allSongs.size} songs")
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val currentSong: StateFlow<Song?> = playbackController.currentSong
    val isPlaying: StateFlow<Boolean> = playbackController.isPlaying
    val currentPosition: StateFlow<Long> = playbackController.currentPosition
    val duration: StateFlow<Long> = playbackController.duration
    val volume: StateFlow<Float> = playbackController.volume
    val systemVolume: StateFlow<Float> = volumeManager.observeVolume()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = if (volumeManager.maxVolume > 0) volumeManager.currentVolume.toFloat() / volumeManager.maxVolume else 0f
        )
    val isShuffleEnabled: StateFlow<Boolean> = playbackController.isShuffleEnabled
    val currentQueue: StateFlow<List<Song>> = playbackController.currentQueue
    val sleepTimerTargetMillis: StateFlow<Long?> = playbackController.sleepTimerTargetMillis
    
    val folders: StateFlow<List<FolderSummary>> = kotlinx.coroutines.flow.combine(
        songRepository.getFolders(),
        songRepository.getFavoriteFolders()
    ) { rawFolders, favorites ->
        rawFolders.map { folder ->
            folder.copy(isFavorite = favorites.contains(folder.folderPath))
        }.sortedWith(
            compareByDescending<FolderSummary> { it.isFavorite }
                .thenBy { it.folderName.lowercase() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    private val _selectedFolderPath = MutableStateFlow<String?>(null)
    val selectedFolderSongs: StateFlow<List<Song>> = kotlinx.coroutines.flow.combine(baseSongs, _selectedFolderPath) { allSongs: List<Song>, path: String? ->
        if (path == null) emptyList()
        else allSongs.filter { java.io.File(it.filePath).parentFile?.absolutePath == path }.sortedBy { it.title }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedFolder(path: String) {
        _selectedFolderPath.value = path
    }

    fun toggleFavoriteFolder(folderPath: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            songRepository.toggleFavoriteFolder(folderPath)
        }
    }

    init {
        viewModelScope.launch {
            songRepository.syncLibrary()
        }
    }

    fun playSong(song: Song, playlist: List<Song> = songs.value) {
        playbackController.playFromPlaylist(song, playlist)
    }

    fun playNext(song: Song) {
        val current = currentSong.value
        playbackController.playNext(song)
        if (current != null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                songRepository.recordQueueAssociation(current.id, song.id)
            }
        }
    }

    fun addToQueue(song: Song) {
        val current = currentSong.value
        playbackController.addToQueue(song)
        if (current != null) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                songRepository.recordQueueAssociation(current.id, song.id)
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playbackController.moveQueueItem(fromIndex, toIndex)
    }

    fun removeFromQueue(index: Int) {
        playbackController.removeQueueItem(index)
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

    fun setSystemVolume(progress: Float) {
        val max = volumeManager.maxVolume
        val targetLevel = (progress * max).toInt()
        volumeManager.setVolume(targetLevel)
    }

    fun startSleepTimer(durationMinutes: Int) {
        playbackController.startSleepTimer(durationMinutes)
    }

    fun cancelSleepTimer() {
        playbackController.cancelSleepTimer()
    }

    companion object {
        fun provideFactory(
            application: SmartShuffleApplication,
            playbackController: PlaybackController
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = application.container
                return LibraryViewModel(
                    container.songRepository, 
                    container.playHistoryDao, 
                    playbackController,
                    SystemVolumeManager(application)
                ) as T
            }
        }
    }
}
