package com.example.joko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_teams")
data class BookmarkTeamEntity(
    @PrimaryKey
    val id: String,
    val teamName: String,
    val eventName: String,
    val ownerId: String,
    val maxCapacity: Int,
    val currentMembersCount: Int,
    val description: String? = null,
    val roleNeed: String? = null, // Stored as comma-separated string
    val ownerContact: String? = null,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
