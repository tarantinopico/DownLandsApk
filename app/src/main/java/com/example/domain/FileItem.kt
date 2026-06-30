package com.example.domain

import android.net.Uri

data class FileItem(
    val id: String,
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val sizeBytes: Long,
    val dateModified: Long,
    val dateAdded: Long,
    val uri: Uri
)
