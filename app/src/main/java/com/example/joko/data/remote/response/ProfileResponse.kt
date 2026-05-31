package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("university") val university: String?,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("portfolio_link") val portfolioLink: String? = null,
    @SerializedName("trust_score") val trustScore: Double? = 0.0,
    @SerializedName("skills") val skills: List<String>? = null,
    @SerializedName("email") val email: String?,
    @SerializedName("linkedin") val linkedin: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean? = false,
    @SerializedName("pfp_url") val pfpUrl: String? = null
)
