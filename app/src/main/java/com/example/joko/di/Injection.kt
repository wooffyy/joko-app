package com.example.joko.di

import android.content.Context
import com.example.joko.data.local.database.AppDatabase
import com.example.joko.data.remote.api.RetrofitClient
import com.example.joko.data.repository.EventRepository

object Injection {
    fun provideRepository(context: Context): EventRepository {
        val database = AppDatabase.getInstance(context)
        val apiService = RetrofitClient.instance
        return EventRepository(apiService, database.eventDao())
    }
}
