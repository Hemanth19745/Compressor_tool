package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressionDao {
    @Query("SELECT * FROM compression_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CompressionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CompressionRecord): Long

    @Query("UPDATE compression_history SET downloadedUriString = :downloadedUri WHERE id = :id")
    suspend fun updateDownloadedUri(id: Int, downloadedUri: String)

    @Query("DELETE FROM compression_history WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM compression_history")
    suspend fun clearAllRecords()
}

@Database(
    entities = [CompressionRecord::class],
    version = 2,
    exportSchema = false
)
abstract class CompressionDatabase : RoomDatabase() {
    abstract fun compressionDao(): CompressionDao

    companion object {
        @Volatile
        private var INSTANCE: CompressionDatabase? = null

        fun getInstance(context: Context): CompressionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompressionDatabase::class.java,
                    "squeeze_studio_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
