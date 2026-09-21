package com.example.smartshuffle.domain

import com.example.smartshuffle.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFolders(): Flow<List<FolderSummary>>
    fun getFavoriteFolders(): Flow<Set<String>>
    suspend fun toggleFavoriteFolder(folderPath: String)
    suspend fun syncLibrary()
    suspend fun recordQueueAssociation(currentSongId: Long, queuedSongId: Long)
    suspend fun getSongById(id: Long): Song?
}
