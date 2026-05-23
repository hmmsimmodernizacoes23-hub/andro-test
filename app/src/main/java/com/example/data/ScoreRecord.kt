package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val songName: String,
    val difficulty: String,
    val score: Int,
    val maxCombo: Int,
    val perfectCount: Int,
    val greatCount: Int,
    val goodCount: Int,
    val missCount: Int,
    val rank: String, // "S", "A", "B", "C", "D", "F"
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)
