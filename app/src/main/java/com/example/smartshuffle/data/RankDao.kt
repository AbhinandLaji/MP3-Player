package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface RankDao {
    @Query("SELECT * FROM song_ranks")
    fun getAllRanks(): List<SongRank>
}
