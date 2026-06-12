package com.example.joko.data.local.dao

import androidx.room.*
import com.example.joko.data.local.entity.BookmarkTeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {

    @Query("SELECT * FROM bookmarked_teams ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkTeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkTeamEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkTeamEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_teams WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>
}
