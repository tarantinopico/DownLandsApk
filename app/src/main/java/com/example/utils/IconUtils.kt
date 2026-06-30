package com.example.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Smartphone" -> Icons.Default.Smartphone
            "AccountBalance" -> Icons.Default.AccountBalance
            "Image" -> Icons.Default.Image
            "Description" -> Icons.Default.Description
            "FolderZip" -> Icons.Default.FolderZip
            "AudioFile" -> Icons.Default.AudioFile
            "VideoFile" -> Icons.Default.VideoFile
            "InsertDriveFile" -> Icons.Default.InsertDriveFile
            "Folder" -> Icons.Default.Folder
            "Code" -> Icons.Default.Code
            "Build" -> Icons.Default.Build
            "Settings" -> Icons.Default.Settings
            else -> Icons.Default.Category
        }
    }
    
    val allIcons = listOf(
        "Smartphone", "AccountBalance", "Image", "Description", "FolderZip", 
        "AudioFile", "VideoFile", "InsertDriveFile", "Folder", "Code", "Build", "Settings"
    )
}
