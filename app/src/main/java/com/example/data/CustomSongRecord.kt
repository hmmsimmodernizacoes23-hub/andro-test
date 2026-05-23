package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_songs")
data class CustomSongRecord(
    @PrimaryKey val songName: String,
    val jsonContent: String,
    val timestamp: Long = System.currentTimeMillis()
)
