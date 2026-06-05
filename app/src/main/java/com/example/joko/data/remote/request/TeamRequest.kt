package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class TeamRequest(
    @SerializedName("team_name") val teamName: String,
    @SerializedName("event_name") val eventName: String,
    @SerializedName("owner_id") val ownerId: String,
    @SerializedName("max_capacity") val maxCapacity: Int,
    @SerializedName("description") val description: String? = null,
    @SerializedName("role_need") val roleNeed: List<String>? = null,
    @SerializedName("owner_contact") val ownerContact: String? = null
)
