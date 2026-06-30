package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_cache")
data class FileCacheEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val sizeBytes: Long,
    val dateModified: Long,
    val dateAdded: Long,
    val uriString: String
)
