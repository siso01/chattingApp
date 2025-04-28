package com.example.chatbot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.model.ChatMessage
import com.example.chatbot.data.repository.ChatRepository
import com.example.chatbot.utils.NetworkMonitor
import com.example.chatbot.utils.SocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val networkMonitor: NetworkMonitor,
    private val socketService: SocketService
) : ViewModel() {
    private val TAG = "ChatViewModel"

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        Log.d(TAG, "Initializing ChatViewModel")
        
        // Connect to socket
        socketService.connect()

        // Monitor network status
        viewModelScope.launch {
            networkMonitor.networkStatus.collectLatest { isConnected ->
                Log.d(TAG, "Network status changed: $isConnected")
                _isConnected.value = isConnected
                if (isConnected) {
                    if (!socketService.isConnected()) {
                        Log.d(TAG, "Reconnecting socket")
                        socketService.connect()
                    }
                    retryPendingMessages()
                } else {
                    Log.d(TAG, "Network disconnected")
                }
            }
        }

        // Listen for messages
        viewModelScope.launch {
            try {
                socketService.listenForMessages().collectLatest { message ->
                    Log.d(TAG, "Received new message: $message")
                    val chatMessage = ChatMessage(
                        content = message,
                        isFromUser = false,
                        isSent = true
                    )
                    repository.insertMessage(chatMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error listening for messages", e)
            }
        }

        // Load existing messages
        viewModelScope.launch {
            repository.getAllMessages().collectLatest { messages ->
                _messages.value = messages
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending message: $content")
                val message = ChatMessage(
                    content = content,
                    isFromUser = true,
                    isSent = _isConnected.value && socketService.isConnected()
                )
                repository.insertMessage(message)
                
                if (_isConnected.value && socketService.isConnected()) {
                    socketService.sendMessage(content)
                } else {
                    Log.d(TAG, "Message queued - not connected")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
            }
        }
    }

    private fun retryPendingMessages() {
        viewModelScope.launch {
            try {
                repository.getPendingMessages().collectLatest { pendingMessages ->
                    Log.d(TAG, "Retrying ${pendingMessages.size} pending messages")
                    pendingMessages.forEach { message ->
                        if (socketService.isConnected() && isConnected.value) {
                            socketService.sendMessage(message.content)
                            val updatedMessage = message.copy(isSent = true)
                            repository.updateMessage(updatedMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying pending messages", e)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearAllMessages()
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketService.disconnect()
    }
} 