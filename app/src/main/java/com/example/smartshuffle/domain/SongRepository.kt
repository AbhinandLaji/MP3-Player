package com.example.smartshuffle.domain

import com.example.smartshuffle.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFolders(): Flow<List<FolderSummary>>
    suspend fun syncLibrary()
}
