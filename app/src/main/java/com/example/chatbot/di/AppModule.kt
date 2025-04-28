package com.example.chatbot.di

import android.content.Context
import com.example.chatbot.data.database.AppDatabase
import com.example.chatbot.data.repository.ChatRepository
import com.example.chatbot.utils.NetworkMonitor
import com.example.chatbot.utils.SocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        database: AppDatabase
    ): ChatRepository {
        return ChatRepository(database.chatDao())
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideSocketService(): SocketService {
        return SocketService()
    }
} 