package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("organizer") val organizer: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("registration_url") val registrationUrl: String? = null,
    @SerializedName("requirements") val requirements: List<String>? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("owner_id") val ownerId: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean? = false,
    @SerializedName("click_count") val clickCount: Int? = 0,
    @SerializedName("trust_score") val trustScore: Double? = 0.0
)
