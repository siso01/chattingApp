package com.example.chatbot.utils

import android.util.Log
import com.piesocket.channels.Channel
import com.piesocket.channels.PieSocket
import com.piesocket.channels.misc.PieSocketEvent
import com.piesocket.channels.misc.PieSocketEventListener
import com.piesocket.channels.misc.PieSocketOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService @Inject constructor() {
    private var piesocket: PieSocket? = null
    var channelPie: Channel? = null
    private val TAG = "SocketService"

    fun connect() {
        try {
            val pieSocketOptions = PieSocketOptions().apply {
                clusterId = "s14547.blr1"
                apiKey = "l12CKsGs3lJ8dwJllAKZMYbmj4o440m1c2UidHwe"
                setNotifySelf(false)
            }

            Log.d(TAG, "Creating PieSocket instance")
            piesocket = PieSocket(pieSocketOptions)
            
            Log.d(TAG, "Joining channel")
            channelPie = piesocket?.join("101")

            channelPie?.listen("system:connected", object : PieSocketEventListener() {
                override fun handleEvent(event: PieSocketEvent) {
                    Log.d(TAG, "Channel connected")
                }
            })

            channelPie?.listen("system:disconnected", object : PieSocketEventListener() {
                override fun handleEvent(event: PieSocketEvent) {
                    Log.d(TAG, "Channel disconnected")
                }
            })

            channelPie?.listen("system:error", object : PieSocketEventListener() {
                override fun handleEvent(event: PieSocketEvent) {
                    Log.e(TAG, "Channel error: ${event.data}")
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to PieSocket", e)
        }
    }

    fun disconnect() {
        try {
            channelPie?.disconnect()
            piesocket = null
            channelPie = null
            Log.d(TAG, "PieSocket disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting PieSocket", e)
        }
    }

    fun sendMessage(message: String) {
        try {
            if (!isConnected()) {
                Log.w(TAG, "Attempting to send message while disconnected")
                return
            }
            
            val event = PieSocketEvent("message")
            event.data = message
            Log.d(TAG, "Sending message: $message")
            channelPie?.publish(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
        }
    }

    fun listenForMessages(): Flow<String> = callbackFlow {
        val listener = object : PieSocketEventListener() {
            override fun handleEvent(event: PieSocketEvent) {
                try {
                    Log.d(TAG, "Received message: ${event.data}")
                    if (event.data is String) {
                        trySend(event.data as String)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing message", e)
                }
            }
        }

        channelPie?.listen("message", listener)
        awaitClose {
            channelPie?.disconnect()
        }
    }

    fun isConnected(): Boolean {
        val connected = channelPie != null
        Log.d(TAG, "PieSocket connection status: $connected")
        return connected
    }
} 