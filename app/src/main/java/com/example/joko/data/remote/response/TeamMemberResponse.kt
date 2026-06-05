package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class TeamMemberResponse(
    @SerializedName("id") val id: String,
    @SerializedName("team_id") val teamId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("status") val status: String, // pending, accepted, rejected
    @SerializedName("joined_at") val joinedAt: String? = null,
    
    // User details if joined in query (for Manage Applicants)
    @SerializedName("users") val userDetails: ProfileResponse? = null,

    // Team details if joined in query (for User's Pending Applications)
    @SerializedName("teams") val teamDetails: TeamResponse? = null
)
