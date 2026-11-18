package com.example.data

import kotlinx.coroutines.flow.Flow

class AudioHistoryRepository(private val dao: AudioHistoryDao) {
    val allHistory: Flow<List<AudioHistoryEntity>> = dao.getAllHistory()

    suspend fun insert(item: AudioHistoryEntity): Long = dao.insertHistory(item)
    suspend fun delete(id: Long) = dao.deleteHistory(id)
    suspend fun rename(id: Long, title: String) = dao.renameHistory(id, title)
    suspend fun clearAll() = dao.clearAllHistory()
}
