package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>
    
    @Query("SELECT * FROM songs")
    suspend fun getAllSongsSync(): List<Song>
    
    @Query("SELECT * FROM songs WHERE filePath = :path LIMIT 1")
    suspend fun getSongByPath(path: String): Song?
    
    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): Song?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: Song): Long
    
    @Update
    suspend fun update(song: Song)
}
