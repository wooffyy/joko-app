package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("university") val university: String?,
    @SerializedName("interests") val interests: List<String>?,
    @SerializedName("portfolio_links") val portfolioLinks: String?
)