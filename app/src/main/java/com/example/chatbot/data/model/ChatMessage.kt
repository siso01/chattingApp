package com.example.chatbot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Date = Date(),
    val isFromUser: Boolean,
    val isSent: Boolean = true,
    val isDelivered: Boolean = false
) 