package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class CreateEventRequest(
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("location") val location: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("organizer") val organizer: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("registration_url") val registrationUrl: String? = null,
    @SerializedName("requirements") val requirements: List<String>? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("owner_id") val ownerId: String?,
    @SerializedName("is_verified") val isVerified: Boolean = true
)
