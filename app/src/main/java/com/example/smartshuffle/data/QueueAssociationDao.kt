package com.example.smartshuffle.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface QueueAssociationDao {
    @Query("SELECT * FROM queue_associations")
    fun getAllAssociations(): List<QueueAssociation>
}
