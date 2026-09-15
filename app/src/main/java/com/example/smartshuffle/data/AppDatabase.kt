package com.example.smartshuffle.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Song::class,
        PlayHistory::class,
        SongRank::class,
        QueueAssociation::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun rankDao(): RankDao
    abstract fun queueAssociationDao(): QueueAssociationDao
}
