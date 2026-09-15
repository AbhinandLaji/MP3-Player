package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history")
    fun getAllHistory(): List<PlayHistory>

    @Query("SELECT songId, COUNT(id) as playCount FROM play_history GROUP BY songId")
    fun getPlayCounts(): Flow<List<PlayCount>>

    @Insert
    suspend fun insert(playHistory: PlayHistory)
}
