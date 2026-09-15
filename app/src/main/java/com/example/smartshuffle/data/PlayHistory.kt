package com.example.smartshuffle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlayType {
    MANUAL,
    SHUFFLE
}

@Entity(tableName = "play_history")
data class PlayHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val timestamp: Long,
    val playType: PlayType
)
