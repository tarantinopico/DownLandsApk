package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FileCacheEntity::class,
        CategoryEntity::class,
        CategoryRuleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileCacheDao(): FileCacheDao
    abstract fun categoryDao(): CategoryDao
}
