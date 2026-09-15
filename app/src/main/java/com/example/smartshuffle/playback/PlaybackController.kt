package com.example.smartshuffle.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.smartshuffle.data.PlayType
import com.example.smartshuffle.data.Song
import com.example.smartshuffle.domain.RankingEngine
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackController(
    private val context: Context,
    private val rankingEngine: RankingEngine
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var currentPlaylist: List<Song> = emptyList()
    
    private val sharedPrefs = context.getSharedPreferences("smartshuffle_prefs", Context.MODE_PRIVATE)

    private val _isShuffleEnabled = MutableStateFlow(sharedPrefs.getBoolean("shuffle_enabled", false))
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                setupController()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun setupController() {
        mediaController?.let { controller ->
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressJob()
                    } else {
                        progressJob?.cancel()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSong(mediaItem)
                    _duration.value = controller.duration.coerceAtLeast(0)

                    val songId = mediaItem?.mediaId?.toLongOrNull()
                    if (songId != null) {
                        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                            scope.launch(Dispatchers.IO) {
                                val playType = if (_isShuffleEnabled.value) PlayType.SHUFFLE else PlayType.MANUAL
                                rankingEngine.incrementRank(songId, playType)
                            }
                        }
                        
                        if (_isShuffleEnabled.value && !controller.hasNextMediaItem()) {
                            scope.launch(Dispatchers.IO) {
                                appendNextShuffleSong(songId)
                            }
                        }
                    }
                }

                override fun onVolumeChanged(volume: Float) {
                    _volume.value = volume
                }
            })
            _volume.value = controller.volume
        }
    }

    private fun updateCurrentSong(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            _currentSong.value = null
            return
        }
        val songId = mediaItem.mediaId.toLongOrNull()
        _currentSong.value = currentPlaylist.find { it.id == songId }
    }

    private fun startProgressJob() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let {
                    _currentPosition.value = it.currentPosition.coerceAtLeast(0)
                }
                delay(1000)
            }
        }
    }
    
    fun toggleShuffle() {
        val newState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newState
        sharedPrefs.edit().putBoolean("shuffle_enabled", newState).apply()
        
        // If we just turned shuffle on, make sure we have a next item in the queue
        if (newState && mediaController?.hasNextMediaItem() == false) {
            _currentSong.value?.let { current ->
                scope.launch(Dispatchers.IO) {
                    appendNextShuffleSong(current.id)
                }
            }
        }
    }

    fun playFromPlaylist(song: Song, playlist: List<Song>) {
        currentPlaylist = playlist
        
        scope.launch(Dispatchers.IO) {
            rankingEngine.incrementRank(song.id, PlayType.MANUAL)
        }
        
        mediaController?.let { controller ->
            if (_isShuffleEnabled.value) {
                val mediaItem = createMediaItem(song)
                controller.setMediaItem(mediaItem)
                
                scope.launch(Dispatchers.IO) {
                    appendNextShuffleSong(song.id)
                }
            } else {
                val startIndex = playlist.indexOf(song).takeIf { it >= 0 } ?: 0
                val mediaItems = playlist.map { createMediaItem(it) }
                controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            }
            controller.prepare()
            controller.play()
        }
    }

    private suspend fun appendNextShuffleSong(currentSongId: Long) {
        val nextSong = rankingEngine.selectNextShuffleSong(excludeSongId = currentSongId)
        nextSong?.let {
            val mediaItem = createMediaItem(it)
            // Switch back to Main thread for ExoPlayer modification
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                mediaController?.addMediaItem(mediaItem)
            }
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.filePath)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { uri -> android.net.Uri.parse(uri) })
                    .build()
            )
            .build()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }
    
    fun queueSongNext(song: Song) {
        mediaController?.let { controller ->
            val mediaItem = createMediaItem(song)
            val nextIndex = controller.currentMediaItemIndex + 1
            controller.addMediaItem(nextIndex, mediaItem)
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _currentPosition.value = position
    }

    fun setVolume(volume: Float) {
        mediaController?.volume = volume
    }

    fun release() {
        progressJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
