package com.example.smartshuffle.playback

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.content.Intent
import android.app.PendingIntent
import com.example.smartshuffle.MainActivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.flow.first
import com.example.smartshuffle.SmartShuffleApplication

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        val app = application as SmartShuffleApplication
        val userPreferences = app.container.userPreferences
        val songRepository = app.container.songRepository

        val player = ExoPlayer.Builder(this).build()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_NOW_PLAYING", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(SessionCallback(userPreferences, songRepository))
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        scope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class SessionCallback(
        private val userPreferences: com.example.smartshuffle.data.UserPreferences,
        private val songRepository: com.example.smartshuffle.domain.SongRepository
    ) : MediaSession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): com.google.common.util.concurrent.ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return scope.future(Dispatchers.IO) {
                val (songId, positionMs) = userPreferences.getPlaybackState()
                if (songId == null || songId == -1L) {
                    throw java.lang.UnsupportedOperationException()
                }
                val allSongs = songRepository.getAllSongs().first()
                val song = allSongs.find { it.id == songId } ?: throw java.lang.UnsupportedOperationException()

                val extras = android.os.Bundle().apply {
                    putString("queue_origin", "MANUAL")
                }
                val mediaItem = androidx.media3.common.MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.filePath)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.albumArtUri?.let { uri -> android.net.Uri.parse(uri) })
                            .setExtras(extras)
                            .build()
                    )
                    .build()

                MediaSession.MediaItemsWithStartPosition(listOf(mediaItem), 0, positionMs ?: 0L)
            }
        }
    }
}
