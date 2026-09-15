package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface QueueAssociationDao {
    @Query("SELECT * FROM queue_associations")
    fun getAllAssociations(): List<QueueAssociation>
    
    @Query("SELECT * FROM queue_associations WHERE songIdA = :songId")
    suspend fun getAssociationsForSong(songId: Long): List<QueueAssociation>
}
