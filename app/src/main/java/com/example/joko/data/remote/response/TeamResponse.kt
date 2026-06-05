package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class TeamResponse(
    @SerializedName("id") val id: String,
    @SerializedName("team_name") val teamName: String,
    @SerializedName("event_name") val eventName: String,
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("max_capacity") val maxCapacity: Int,
    @SerializedName("description") val description: String? = null,
    @SerializedName("role_need") val roleNeed: List<String>? = null,
    @SerializedName("owner_contact") val ownerContact: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("current_members_count") val currentMembersCount: Int = 0
)
