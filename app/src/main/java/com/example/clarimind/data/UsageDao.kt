package com.example.clarimind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE date = :date")
    suspend fun getUsagesForDate(date: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getUsagesBetweenDates(startDate: String, endDate: String): List<AppUsageEntity>
} 