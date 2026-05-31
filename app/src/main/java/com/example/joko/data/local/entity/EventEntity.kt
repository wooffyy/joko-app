package com.example.joko.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [
        Index(value = ["startDate"]),
        Index(value = ["ownerId"]),
        Index(value = ["isVerified"])
    ]
)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val organizer: String,
    val category: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val description: String,
    val imageUrl: String,
    val registrationUrl: String? = null,
    val tags: String? = null,
    val requirements: String? = null,
    val ownerId: String?,
    val clickCount: Int = 0,
    val isVerified: Boolean = false,
    val trustScore: Double = 0.0
)
