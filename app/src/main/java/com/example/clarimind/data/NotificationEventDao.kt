package com.example.clarimind.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NotificationEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NotificationEventEntity)

    @Query("SELECT * FROM notification_events ORDER BY postTime DESC")
    suspend fun getAllEvents(): List<NotificationEventEntity>
} 