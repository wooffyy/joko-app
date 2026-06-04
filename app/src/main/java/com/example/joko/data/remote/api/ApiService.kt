package com.example.joko.data.remote.api

import com.example.joko.data.remote.request.AuthRequest
import com.example.joko.data.remote.request.CreateEventRequest
import com.example.joko.data.remote.request.CreateUserRequest
import com.example.joko.data.remote.request.UpdateProfileRequest
import com.example.joko.data.remote.response.AuthResponse
import com.example.joko.data.remote.response.EventResponse
import com.example.joko.data.remote.response.ProfileResponse
import com.example.joko.data.remote.response.UserResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("rest/v1/events?select=*&owner_id=not.is.null")
    suspend fun getEvents(
        @Query("order") order: String = "start_date.desc"
    ): List<EventResponse>

    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: AuthRequest): AuthResponse

    @POST("rest/v1/users")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=minimal",
        @Body request: CreateUserRequest
    ): Response<Unit>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body request: AuthRequest): AuthResponse

    @GET("auth/v1/user")
    suspend fun getCurrentUser(): UserResponse

    @POST("rest/v1/events")
    suspend fun createEvent(
        @Header("Prefer") prefer: String = "return=minimal",
        @Body request: CreateEventRequest
    ): Response<Unit>

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadImage(
        @Path("bucket") bucket: String,
        @Path("path") path: String,
        @Body image: RequestBody,
        @Header("Content-Type") contentType: String = "image/jpeg"
    ): Response<Unit>

    @GET("rest/v1/users")
    suspend fun getUserProfile(
        @Query("id") userId: String
    ): List<ProfileResponse>

    @PATCH("rest/v1/users")
    suspend fun updateProfile(
        @Query("id") userId: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}
