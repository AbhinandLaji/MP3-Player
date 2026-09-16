package com.example.smartshuffle.data

import android.content.Context
import com.example.smartshuffle.domain.FolderSummary
import com.example.smartshuffle.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
    private val songDao: SongDao,
    private val queueAssociationDao: QueueAssociationDao,
    private val context: Context
) : SongRepository {
    
    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs()
    }

    override fun getFolders(): Flow<List<FolderSummary>> {
        return songDao.getAllSongs().map { songs ->
            songs.groupBy { 
                val file = java.io.File(it.filePath)
                file.parentFile?.absolutePath ?: "Unknown"
            }.map { (path, folderSongs) ->
                val folderName = java.io.File(path).name
                FolderSummary(
                    folderName = folderName,
                    folderPath = path,
                    songCount = folderSongs.size
                )
            }.sortedBy { it.folderName }
        }
    }

    override suspend fun syncLibrary() {
        val scannedSongs = MusicScanner.queryMediaStore(context)
        
        for (song in scannedSongs) {
            val existingSong = songDao.getSongByPath(song.filePath)
            if (existingSong == null) {
                songDao.insert(song)
            } else {
                // Update existing song metadata if needed
                val updatedSong = existingSong.copy(
                    title = song.title,
                    artist = song.artist,
                    album = song.album,
                    duration = song.duration,
                    albumArtUri = song.albumArtUri
                )
                songDao.update(updatedSong)
            }
        }
    }

    override suspend fun recordQueueAssociation(currentSongId: Long, queuedSongId: Long) {
        if (currentSongId == queuedSongId || currentSongId <= 0L) return
        queueAssociationDao.incrementAssociationCount(currentSongId, queuedSongId)
    }
}
