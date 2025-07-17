package com.example.clarimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val sender: String, // "user" or "bot"
    val message: String,
    val timestamp: Long
) 