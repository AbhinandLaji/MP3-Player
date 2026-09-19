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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackController(
    private val context: Context,
    private val rankingEngine: RankingEngine,
    private val userPreferences: com.example.smartshuffle.data.UserPreferences
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

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _sleepTimerTargetMillis = MutableStateFlow<Long?>(null)
    val sleepTimerTargetMillis: StateFlow<Long?> = _sleepTimerTargetMillis.asStateFlow()

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
        
        scope.launch {
            userPreferences.sleepTimerTargetFlow.collect { target ->
                if (target != null) {
                    if (target > System.currentTimeMillis()) {
                        _sleepTimerTargetMillis.value = target
                    } else {
                        userPreferences.clearSleepTimerTarget()
                        _sleepTimerTargetMillis.value = null
                    }
                } else {
                    _sleepTimerTargetMillis.value = null
                }
            }
        }
    }

    private fun setupController() {
        mediaController?.let { controller ->
            controller.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == androidx.media3.common.Player.STATE_READY) {
                        _duration.value = controller.duration.coerceAtLeast(0L)
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressJob()
                    } else {
                        progressJob?.cancel()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        val target = _sleepTimerTargetMillis.value
                        if (target != null && System.currentTimeMillis() >= target) {
                            controller.pause()
                            cancelSleepTimer()
                            return
                        }
                    }

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
                        
                        maintainQueueBuffer()
                    }
                }

                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                    super.onTimelineChanged(timeline, reason)
                    updateQueueState()
                }

                override fun onVolumeChanged(volume: Float) {
                    _volume.value = volume
                }
            })
            _volume.value = controller.volume
            
            if (controller.isPlaying) {
                _isPlaying.value = true
                startProgressJob()
            }
            updateQueueState()
        }
    }

    private fun updateQueueState() {
        mediaController?.let { controller ->
            val newQueue = mutableListOf<Song>()
            val currentIndex = controller.currentMediaItemIndex
            for (i in currentIndex until controller.mediaItemCount) {
                val mediaItem = controller.getMediaItemAt(i)
                val songId = mediaItem.mediaId.toLongOrNull()
                val song = currentPlaylist.find { it.id == songId }
                if (song != null) {
                    newQueue.add(song)
                }
            }
            _currentQueue.value = newQueue
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
            while (isActive && mediaController?.isPlaying == true) {
                mediaController?.let {
                    _currentPosition.value = it.currentPosition.coerceAtLeast(0)
                }
                delay(200L)
            }
        }
    }
    
    fun toggleShuffle() {
        val newState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newState
        sharedPrefs.edit().putBoolean("shuffle_enabled", newState).apply()
        
        if (newState) {
            // If we just turned shuffle on, make sure our buffer is full
            maintainQueueBuffer()
        } else if (!newState) {
            // We just turned shuffle off. We need to restore the sequential queue tail 
            // from the current playlist so sequential playback can resume, 
            // without wiping any manually queued items that might be next.
            mediaController?.let { controller ->
                _currentSong.value?.let { current ->
                    val startIndex = currentPlaylist.indexOf(current)
                    if (startIndex >= 0 && startIndex < currentPlaylist.size - 1) {
                        val remainingSongs = currentPlaylist.subList(startIndex + 1, currentPlaylist.size)
                        
                        // Check if we need to append. If the queue is already very large, 
                        // it likely still contains the sequential tail from when it started.
                        val remainingInPlayer = controller.mediaItemCount - 1 - controller.currentMediaItemIndex
                        if (remainingInPlayer < remainingSongs.size) {
                            val mediaItems = remainingSongs.map { createMediaItem(it) }
                            controller.addMediaItems(mediaItems)
                        }
                    }
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
                maintainQueueBuffer()
            } else {
                val startIndex = playlist.indexOf(song).takeIf { it >= 0 } ?: 0
                val mediaItems = playlist.map { createMediaItem(it) }
                controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            }
            controller.prepare()
            controller.play()
        }
    }

    private val TARGET_UPCOMING_BUFFER = 5

    private fun maintainQueueBuffer() {
        val controller = mediaController ?: return
        val upcomingCount = (controller.mediaItemCount - 1) - controller.currentMediaItemIndex
        if (_isShuffleEnabled.value && upcomingCount < TARGET_UPCOMING_BUFFER) {
            val needed = TARGET_UPCOMING_BUFFER - upcomingCount
            scope.launch(Dispatchers.IO) {
                val baseSongId = _currentSong.value?.id ?: return@launch
                repeat(needed) {
                    val nextSong = rankingEngine.selectNextShuffleSong(
                        currentSongId = baseSongId,
                        playlistContext = currentPlaylist
                    )
                    nextSong?.let {
                        val mediaItem = createMediaItem(it)
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            controller.addMediaItem(mediaItem)
                        }
                    }
                }
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
    
    fun playNext(song: Song) {
        mediaController?.let { controller ->
            val mediaItem = createMediaItem(song)
            val nextIndex = (controller.currentMediaItemIndex + 1).coerceAtMost(controller.mediaItemCount)
            controller.addMediaItem(nextIndex, mediaItem)
            if (!currentPlaylist.contains(song)) {
                currentPlaylist = currentPlaylist + song
            }
            updateQueueState()
        }
    }

    fun addToQueue(song: Song) {
        mediaController?.let { controller ->
            controller.addMediaItem(createMediaItem(song))
            if (!currentPlaylist.contains(song)) {
                currentPlaylist = currentPlaylist + song
            }
            updateQueueState()
        }
    }

    fun moveQueueItem(fromDisplayIndex: Int, toDisplayIndex: Int) {
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex

        // Offset display indices to match ExoPlayer's absolute timeline indices
        // from/to display indices are 0-based in "Up Next" (drop(1))
        val fromAbsolute = currentIndex + 1 + fromDisplayIndex
        val toAbsolute = currentIndex + 1 + toDisplayIndex

        if (fromAbsolute in (currentIndex + 1) until controller.mediaItemCount &&
            toAbsolute in (currentIndex + 1) until controller.mediaItemCount) {
            controller.moveMediaItem(fromAbsolute, toAbsolute)
            updateQueueState()
        }
    }

    fun removeQueueItem(displayIndex: Int) {
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex
        val absoluteIndex = currentIndex + 1 + displayIndex
        if (absoluteIndex in (currentIndex + 1) until controller.mediaItemCount) {
            controller.removeMediaItem(absoluteIndex)
            updateQueueState()
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
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }

    fun startSleepTimer(durationMinutes: Int) {
        val target = System.currentTimeMillis() + (durationMinutes * 60_000L)
        _sleepTimerTargetMillis.value = target
        scope.launch(Dispatchers.IO) {
            userPreferences.setSleepTimerTarget(target)
        }
    }

    fun cancelSleepTimer() {
        _sleepTimerTargetMillis.value = null
        scope.launch(Dispatchers.IO) {
            userPreferences.clearSleepTimerTarget()
        }
    }
}
