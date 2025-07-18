package com.example.clarimind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HappinessHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: HappinessHistoryEntity)

    @Query("SELECT * FROM happiness_history WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getHistoryForUser(userId: String): List<HappinessHistoryEntity>

    @Query("SELECT * FROM happiness_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HappinessHistoryEntity>
} 