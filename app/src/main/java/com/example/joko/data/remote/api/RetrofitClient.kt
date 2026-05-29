package com.example.joko.data.remote.api

import android.content.Context
import android.util.Log
import com.example.joko.BuildConfig
import com.example.joko.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    private var apiService: ApiService? = null
    
    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            val instance = createApiService(context)
            apiService = instance
            instance
        }
    }

    private fun createApiService(context: Context): ApiService {
        val sessionManager = SessionManager(context)
        
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                // cek token user, kalo ga ada fallback ke anon key bikin token baru
                val token = sessionManager.getAuthToken() ?: BuildConfig.SUPABASE_ANON_KEY
                
                val request = chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val baseUrl = BuildConfig.SUPABASE_URL
        
        val finalUrl = when {
            baseUrl.isBlank() -> {
                Log.e(TAG, "SUPABASE_URL is empty! Check local.properties")
                "https://placeholder.supabase.co/"
            }
            !baseUrl.startsWith("http") -> {
                Log.e(TAG, "SUPABASE_URL must start with http/https: $baseUrl")
                "https://placeholder.supabase.co/"
            }
            baseUrl.endsWith("/") -> baseUrl
            else -> "$baseUrl/"
        }

        return try {
            Retrofit.Builder()
                .baseUrl(finalUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Retrofit: ${e.message}")
            throw e
        }
    }
}
