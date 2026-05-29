package com.example.joko.di

import android.content.Context
import com.example.joko.data.local.database.AppDatabase
import com.example.joko.data.remote.api.RetrofitClient
import com.example.joko.data.repository.AuthRepository
import com.example.joko.data.repository.EventRepository
import com.example.joko.utils.SessionManager

object Injection {
    fun provideRepository(context: Context): EventRepository {
        val database = AppDatabase.getInstance(context)
        val apiService = RetrofitClient.getApiService(context)
        return EventRepository(apiService, database.eventDao())
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        val apiService = RetrofitClient.getApiService(context)
        val sessionManager = SessionManager(context)
        return AuthRepository(apiService, sessionManager)
    }
}
