package com.example.joko.data.repository

import com.example.joko.data.local.dao.EventDao
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.data.remote.api.ApiService
import com.example.joko.data.remote.response.EventResponse
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun refreshEvents() {
        try {
            val response = apiService.getEvents()
            val entities = response.map { it.toEntity() }
            eventDao.insertEvents(entities)
        } catch (e: Exception) {
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
            deadline = event.deadline,
            description = event.description,
            imageUrl = event.imageUrl
        )
        eventDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(bookmark: BookmarkEventEntity) {
        eventDao.deleteBookmark(bookmark)
    }

    private fun EventResponse.toEntity(): EventEntity {
        return EventEntity(id, title, organizer, category, location, deadline, description, imageUrl)
    }
}
