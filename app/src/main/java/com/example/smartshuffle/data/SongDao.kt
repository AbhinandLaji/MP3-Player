package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): List<Song>
}
