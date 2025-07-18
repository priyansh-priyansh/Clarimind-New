package com.example.clarimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happiness_history")
data class HappinessHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val mood: String,
    val rememberedWellBeing: Double,
    val experiencedWellBeing: Double,
    val combinedPHI: Double,
    val timestamp: Long
) 