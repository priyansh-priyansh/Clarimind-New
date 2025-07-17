package com.example.clarimind.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppUsageEntity::class, NotificationEventEntity::class, ChatMessageEntity::class], version = 3)
abstract class UsageDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
    abstract fun notificationEventDao(): NotificationEventDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile private var INSTANCE: UsageDatabase? = null

        fun getInstance(context: Context): UsageDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UsageDatabase::class.java,
                    "usage_db"
                ).build().also { INSTANCE = it }
            }
    }
} 