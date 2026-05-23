package com.example.data

import kotlinx.coroutines.flow.Flow

class CustomSongRepository(private val customSongDao: CustomSongDao) {
    val allCustomSongs: Flow<List<CustomSongRecord>> = customSongDao.getAllCustomSongs()

    suspend fun saveCustomSong(customSong: CustomSongRecord) {
        customSongDao.insertCustomSong(customSong)
    }

    suspend fun deleteCustomSong(songName: String) {
        customSongDao.deleteCustomSongByName(songName)
    }

    suspend fun clearAllCustomSongs() {
        customSongDao.clearAllCustomSongs()
    }
}
