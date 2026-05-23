package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScoreRecord::class], version = 1, exportSchema = false)
abstract class RhythmDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: RhythmDatabase? = null

        fun getDatabase(context: Context): RhythmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RhythmDatabase::class.java,
                    "rhythm_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
