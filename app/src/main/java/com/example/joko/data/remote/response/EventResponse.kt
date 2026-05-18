package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("organizer") val organizer: String,
    @SerializedName("category") val category: String,
    @SerializedName("location") val location: String,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("description") val description: String,
    @SerializedName("image_url") val imageUrl: String
)
