package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_history")
data class AudioHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val text: String,
    val languageCode: String,
    val voiceName: String,
    val gender: String,
    val speed: Float,
    val pitch: Float,
    val filePath: String,
    val durationMs: Long,
    val createdAt: Long = System.currentTimeMillis()
)
