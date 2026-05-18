package com.example.joko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_events")
data class BookmarkEventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val location: String,
    val deadline: String,
    val description: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)
