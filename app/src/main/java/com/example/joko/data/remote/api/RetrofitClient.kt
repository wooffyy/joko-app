package com.example.joko.data.remote.api

import android.util.Log
import com.example.joko.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val TAG = "RetrofitClient"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: ApiService by lazy {
        val baseUrl = BuildConfig.SUPABASE_URL
        
        // Validasi URL sebelum inisialisasi Retrofit
        val finalUrl = when {
            baseUrl.isBlank() -> {
                Log.e(TAG, "SUPABASE_URL is empty! Check local.properties")
                "https://placeholder.supabase.co/" // Fallback agar tidak crash saat init
            }
            !baseUrl.startsWith("http") -> {
                Log.e(TAG, "SUPABASE_URL must start with http/https: $baseUrl")
                "https://placeholder.supabase.co/"
            }
            baseUrl.endsWith("/") -> baseUrl
            else -> "$baseUrl/"
        }

        try {
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
