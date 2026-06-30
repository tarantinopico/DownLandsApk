package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FileCacheDao {
    @Query("SELECT * FROM file_cache ORDER BY dateModified DESC")
    fun getAllFiles(): Flow<List<FileCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileCacheEntity>)

    @Query("DELETE FROM file_cache")
    suspend fun clearAll()
}
