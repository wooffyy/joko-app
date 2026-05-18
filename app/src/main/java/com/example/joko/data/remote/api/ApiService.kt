package com.example.joko.data.remote.api

import com.example.joko.data.remote.response.EventResponse
import retrofit2.http.GET

interface ApiService {
    @GET("events") // Endpoint dummy
    suspend fun getEvents(): List<EventResponse>
}
