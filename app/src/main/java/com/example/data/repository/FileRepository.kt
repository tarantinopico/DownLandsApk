package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.data.local.FileCacheDao
import com.example.data.local.FileCacheEntity
import com.example.domain.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FileRepository(
    private val context: Context,
    private val fileCacheDao: FileCacheDao
) {
    val allFiles: Flow<List<FileItem>> = fileCacheDao.getAllFiles().map { entities ->
        entities.map {
            FileItem(
                id = it.id,
                displayName = it.displayName,
                extension = it.extension,
                mimeType = it.mimeType,
                sizeBytes = it.sizeBytes,
                dateModified = it.dateModified,
                dateAdded = it.dateAdded,
                uri = Uri.parse(it.uriString)
            )
        }
    }

    suspend fun refreshFiles(treeUriString: String) = withContext(Dispatchers.IO) {
        val treeUri = Uri.parse(treeUriString)
        val documentFile = DocumentFile.fromTreeUri(context, treeUri)
        
        if (documentFile != null && documentFile.exists() && documentFile.isDirectory) {
            val fileEntities = mutableListOf<FileCacheEntity>()
            
            // For a flat scan of the downloads directory
            documentFile.listFiles().forEach { file ->
                if (file.isFile) {
                    val displayName = file.name ?: "Unknown"
                    val extension = displayName.substringAfterLast('.', "")
                    fileEntities.add(
                        FileCacheEntity(
                            id = file.uri.toString(),
                            displayName = displayName,
                            extension = extension,
                            mimeType = file.type ?: "*/*",
                            sizeBytes = file.length(),
                            dateModified = file.lastModified(),
                            dateAdded = file.lastModified(), // DocumentFile doesn't expose dateAdded, so use modified
                            uriString = file.uri.toString()
                        )
                    )
                }
            }
            
            fileCacheDao.clearAll()
            fileCacheDao.insertAll(fileEntities)
        }
    }
}
