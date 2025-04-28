package com.example.chatbot.data.repository

import com.example.chatbot.data.dao.ChatDao
import com.example.chatbot.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
) {
    fun getAllMessages(): Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage) = chatDao.insertMessage(message)

    suspend fun insertMessages(messages: List<ChatMessage>) = chatDao.insertMessages(messages)

    fun getPendingMessages(): Flow<List<ChatMessage>> = chatDao.getPendingMessages()

    suspend fun updateMessage(message: ChatMessage) = chatDao.updateMessage(message)

    suspend fun clearAllMessages() = chatDao.clearAllMessages()
} 