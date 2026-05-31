package com.example.joko.data.local.dao

import androidx.room.*
import com.example.joko.data.local.entity.BookmarkEventEntity
import com.example.joko.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // 1. Tambahkan filter aktif agar event tanpa owner atau yang sudah lewat tidak ditarik
    @Query("SELECT * FROM events WHERE ownerId IS NOT NULL ORDER BY startDate DESC")
    fun getAllActiveEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: String): Flow<EventEntity?>

    // 2. Gunakan OnConflictStrategy.REPLACE dengan bijak untuk Upsert individu/list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEvents(events: List<EventEntity>)

    // 3. SELEKTIF DELETE (Bukan hapus semua). Hapus hanya yang ID-nya sudah tidak valid dari Server
    @Query("DELETE FROM events WHERE id NOT IN (:serverIds)")
    suspend fun deleteOrphanEvents(serverIds: List<String>)

    // 4. Pengganti replaceEvents yang jauh lebih aman bagi performa disk & UI
    @Transaction
    suspend fun syncEventsFromServer(eventsFromServer: List<EventEntity>) {
        // Masukkan data baru / update data lama
        insertOrUpdateEvents(eventsFromServer)

        // Hapus data lokal yang di server ternyata sudah dihapus
        val serverIds = eventsFromServer.map { it.id }
        deleteOrphanEvents(serverIds)
    }

    @Query("UPDATE events SET clickCount = clickCount + 1 WHERE id = :id")
    suspend fun incrementLocalClickCount(id: String)

    @Query("SELECT * FROM bookmarked_events ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEventEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEventEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_events WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>
}
