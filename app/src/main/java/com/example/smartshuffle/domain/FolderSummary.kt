package com.example.smartshuffle.domain

data class FolderSummary(
    val folderName: String,
    val folderPath: String,
    val songCount: Int,
    val isFavorite: Boolean = false
)
