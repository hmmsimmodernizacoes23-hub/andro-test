package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSongDao {
    @Query("SELECT * FROM custom_songs ORDER BY timestamp DESC")
    fun getAllCustomSongs(): Flow<List<CustomSongRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSong(customSong: CustomSongRecord)

    @Query("DELETE FROM custom_songs WHERE songName = :songName")
    suspend fun deleteCustomSongByName(songName: String)

    @Query("DELETE FROM custom_songs")
    suspend fun clearAllCustomSongs()
}
