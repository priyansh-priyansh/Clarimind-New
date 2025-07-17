package com.example.clarimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val usageTime: Long, // in minutes
    val date: String // e.g., "2024-06-09"
) 