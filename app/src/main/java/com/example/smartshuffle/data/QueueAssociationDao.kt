package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface QueueAssociationDao {
    @Query("SELECT * FROM queue_associations")
    fun getAllAssociations(): List<QueueAssociation>
    
    @Query("SELECT * FROM queue_associations WHERE songIdA = :songId")
    suspend fun getAssociationsForSong(songId: Long): List<QueueAssociation>

    @Query("INSERT INTO queue_associations (songIdA, songIdB, count) VALUES (:songIdA, :songIdB, 1) ON CONFLICT(songIdA, songIdB) DO UPDATE SET count = count + 1")
    suspend fun incrementAssociationCount(songIdA: Long, songIdB: Long)
}
