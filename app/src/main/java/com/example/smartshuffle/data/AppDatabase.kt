package com.example.smartshuffle.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun toPlayType(value: String) = enumValueOf<PlayType>(value)

    @TypeConverter
    fun fromPlayType(value: PlayType) = value.name
}

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
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun rankDao(): RankDao
    abstract fun queueAssociationDao(): QueueAssociationDao
}
