package com.example.joko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val location: String,
    val deadline: String,
    val description: String,
    val imageUrl: String
)
