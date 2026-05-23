package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val allScores: Flow<List<ScoreRecord>> = scoreDao.getAllScores()

    fun getHighScore(songName: String): Flow<ScoreRecord?> {
        return scoreDao.getHighScoreForSong(songName)
    }

    suspend fun saveScore(score: ScoreRecord) {
        scoreDao.insertScore(score)
    }

    suspend fun clearScores() {
        scoreDao.clearAllScores()
    }
}
