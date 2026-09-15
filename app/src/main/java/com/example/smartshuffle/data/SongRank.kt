package com.example.smartshuffle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_ranks")
data class SongRank(
    @PrimaryKey val songId: Long,
    val rankValue: Float,
    val lastUpdated: Long
)
