package com.example.smartshuffle.data

import androidx.room.Entity

@Entity(tableName = "queue_associations", primaryKeys = ["songIdA", "songIdB"])
data class QueueAssociation(
    val songIdA: Long,
    val songIdB: Long,
    val count: Int
)
