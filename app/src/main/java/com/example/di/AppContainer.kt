package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.FileRepository
import com.example.data.repository.CategoryRepository

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "downlands_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val fileRepository: FileRepository by lazy {
        FileRepository(context, database.fileCacheDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val backupManager: com.example.data.repository.BackupManager by lazy {
        com.example.data.repository.BackupManager(categoryRepository, settingsRepository)
    }
}
