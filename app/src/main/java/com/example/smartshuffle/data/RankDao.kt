package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RankDao {
    @Query("SELECT * FROM song_ranks")
    fun getAllRanks(): List<SongRank>

    @Query("SELECT * FROM song_ranks WHERE songId = :songId LIMIT 1")
    suspend fun getRank(songId: Long): SongRank?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(songRank: SongRank)
}
