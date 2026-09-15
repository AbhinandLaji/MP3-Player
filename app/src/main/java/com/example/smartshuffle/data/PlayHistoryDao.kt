package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history")
    fun getAllHistory(): List<PlayHistory>
}
