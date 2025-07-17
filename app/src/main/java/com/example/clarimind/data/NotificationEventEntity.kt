package com.example.clarimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long
) 