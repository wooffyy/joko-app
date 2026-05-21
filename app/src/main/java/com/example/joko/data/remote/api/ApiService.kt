package com.example.joko.data.remote.api

import com.example.joko.data.remote.request.AuthRequest
import com.example.joko.data.remote.response.AuthResponse
import com.example.joko.data.remote.response.EventResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("rest/v1/events?select=*")
    suspend fun getEvents(): List<EventResponse>

    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: AuthRequest): AuthResponse

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body request: AuthRequest): AuthResponse
}
