package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<ScoreRecord>>

    @Query("SELECT * FROM scores WHERE songName = :songName ORDER BY score DESC LIMIT 1")
    fun getHighScoreForSong(songName: String): Flow<ScoreRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecord)

    @Query("DELETE FROM scores")
    suspend fun clearAllScores()
}
