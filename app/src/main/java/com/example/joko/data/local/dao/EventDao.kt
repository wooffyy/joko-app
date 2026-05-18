package com.example.joko.data.local.dao

import androidx.room.*
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM bookmarked_events ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEventEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEventEntity)

    @Query("SELECT EXISTS(SELECT * FROM bookmarked_events WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>
}
