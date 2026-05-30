package com.example.joko.data.repository

import android.util.Log
import com.example.joko.data.local.dao.EventDao
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.CreateEventRequest
import com.example.joko.data.remote.response.EventResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class EventRepository(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    private val TAG = "EventRepository"

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEventById(id: String): Flow<EventEntity?> = eventDao.getEventById(id)

    suspend fun refreshEvents() {
        try {
            val response = apiService.getEvents()
            val entities = response.map { it.toEntity() }
            // Clean Sync Strategy: Hapus data lama dan masukkan data terbaru dari server dalam satu transaksi
            eventDao.replaceEvents(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Refresh Events Error: ${e.message}")
            throw e
        }
    }

    suspend fun publishEvent(request: CreateEventRequest): Response<Unit> {
        Log.d(TAG, "Publishing Event to Supabase...")
        Log.d(TAG, "Request Body: $request")
        
        return try {
            val response = apiService.createEvent(request = request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP ${response.code()} Failure: $errorBody")
                throw Exception(errorBody ?: "Unknown API Error")
            }
            Log.d(TAG, "Publish Successful!")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Publish Event Exception: ${e.message}")
            throw e
        }
    }

    val allBookmarks: Flow<List<BookmarkEventEntity>> = eventDao.getAllBookmarks()

    fun isBookmarked(id: String): Flow<Boolean> = eventDao.isBookmarked(id)

    suspend fun addBookmark(event: EventEntity) {
        val bookmark = BookmarkEventEntity(
            id = event.id,
            title = event.title,
            organizer = event.organizer,
            category = event.category,
            location = event.location,
            startDate = event.startDate,
            endDate = event.endDate,
            description = event.description,
            imageUrl = event.imageUrl,
            registrationUrl = event.registrationUrl,
            tags = event.tags,
            requirements = event.requirements,
            ownerId = event.ownerId
        )
        eventDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(bookmark: BookmarkEventEntity) {
        eventDao.deleteBookmark(bookmark)
    }

    private fun EventResponse.toEntity(): EventEntity {
        return EventEntity(
            id = id,
            title = title,
            organizer = organizer ?: "",
            category = category ?: "",
            location = location ?: "",
            startDate = startDate ?: "",
            endDate = endDate ?: "",
            description = description ?: "",
            imageUrl = imageUrl ?: "",
            registrationUrl = registrationUrl,
            tags = tags?.joinToString(","), // Map List to String for Room
            requirements = requirements?.joinToString(","), // Map List to String for Room
            ownerId = ownerId ?: ""
        )
    }
}
