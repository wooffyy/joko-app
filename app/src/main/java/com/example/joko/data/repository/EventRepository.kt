package com.example.joko.data.repository

import android.util.Log
import com.example.joko.BuildConfig
import com.example.joko.data.local.dao.EventDao
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.request.CreateEventRequest
import com.example.joko.data.remote.request.ReportEventRequest
import com.example.joko.data.remote.request.ReportTeamRequest
import com.example.joko.data.remote.response.EventResponse
import com.example.joko.utils.ReportType
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class EventRepository(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    private val TAG = "EventRepository"

    companion object {
        private const val EVENT_IMAGES_BUCKET = "event-images"
    }

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllActiveEvents()

    fun getEventById(id: String): Flow<EventEntity?> = eventDao.getEventById(id)

    suspend fun refreshEvents() {
        try {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val response = apiService.getEvents(endDateFilter = "gte.$today")

            // Konversi ke entity, data orphan/null ditangani di toEntity()
            val entities = response.map { it.toEntity() }

            eventDao.syncEventsFromServer(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Refresh Events Error: ${e.message}")
            throw e
        }
    }

    suspend fun publishEvent(request: CreateEventRequest): Response<Unit> {
        Log.d(TAG, "Publishing Event to Supabase...")
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

    suspend fun uploadEventBanner(ownerId: String, imageBytes: ByteArray): String {
        val bucket = EVENT_IMAGES_BUCKET
        val fileName = "${System.currentTimeMillis()}_${ownerId}.jpg"
        val path = "banners/$fileName"

        return try {
            Log.d(TAG, "Uploading image to $bucket/$path...")
            val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val response = apiService.uploadImage(bucket, path, requestBody)

            if (response.isSuccessful) {
                val publicUrl = "${BuildConfig.SUPABASE_URL}/storage/v1/object/public/$bucket/$path"
                Log.d(TAG, "Upload Successful. Public URL: $publicUrl")
                publicUrl
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Upload Failed! HTTP ${response.code()}: $errorBody")
                throw Exception("Upload failed: $errorBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload Exception: ${e.message}")
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
            ownerId = event.ownerId,
            isVerified = event.isVerified,
            trustScore = event.trustScore
        )
        eventDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(bookmark: BookmarkEventEntity) {
        eventDao.deleteBookmark(bookmark)
    }

    // Mapping logis dan pengamanan data null dari network
    private fun EventResponse.toEntity(): EventEntity {
        return EventEntity(
            id = id,
            title = title,
            organizer = organizer ?: "Unknown Organizer",
            category = category ?: "General",
            location = location ?: "Online / TBD",
            startDate = startDate ?: "",
            endDate = endDate ?: "",
            description = description ?: "",
            imageUrl = imageUrl ?: "",
            registrationUrl = registrationUrl,
            tags = tags?.joinToString(","),
            requirements = requirements?.joinToString(","),
            ownerId = ownerId ?: "",
            isVerified = isVerified ?: false,
            trustScore = trustScore ?: 0.0
        )
    }

    suspend fun reportEvent(request: ReportEventRequest): Response<Unit> {
        return try {
            val response = apiService.sendReportEvent(request = request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    val code = json.optString("code")
                    when (code) {
                        "23505" -> "Kamu sudah mengirim laporan yang sama tentang event ini sebelumnya."
                        else -> json.optString("message", "Gagal mengirim laporan")
                    }
                } catch (e: Exception) {
                    "Terjadi kesalahan server (${response.code()})"
                }
                throw Exception(errorMessage)
            }
            response
        } catch (e: Exception) {
            // network error
            if (e is java.io.IOException) {
                throw Exception("Koneksi internet bermasalah")
            }
            throw e
        }
    }
}
