package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("university") val university: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("portfolio_link") val portfolioLink: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("linkedin") val linkedin: String?,
    @SerializedName("skills") val skills: List<String>?,
    @SerializedName("pfp_url") val pfpUrl: String? = null
)
