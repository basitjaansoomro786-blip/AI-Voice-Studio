package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioHistoryDao {
    @Query("SELECT * FROM audio_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<AudioHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: AudioHistoryEntity): Long

    @Query("DELETE FROM audio_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("UPDATE audio_history SET title = :newTitle WHERE id = :id")
    suspend fun renameHistory(id: Long, newTitle: String)

    @Query("DELETE FROM audio_history")
    suspend fun clearAllHistory()
}
