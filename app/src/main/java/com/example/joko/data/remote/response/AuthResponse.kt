package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") val accessTokenTop: String? = null,
    @SerializedName("refresh_token") val refreshTokenTop: String? = null,
    @SerializedName("session") val session: SessionResponse? = null,
    @SerializedName("user") val user: UserResponse? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null
) {
    val accessToken: String? get() = accessTokenTop ?: session?.accessToken
    val refreshToken: String? get() = refreshTokenTop ?: session?.refreshToken
    val userId: String? get() = user?.id ?: session?.user?.id ?: id
    val fullName: String? get() = user?.userMetadata?.get("full_name") as? String ?: session?.user?.userMetadata?.get("full_name") as? String
}

data class SessionResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user") val user: UserResponse? = null
)

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("user_metadata") val userMetadata: Map<String, Any>? = null,
    @SerializedName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerializedName("last_sign_in_at") val lastSignInAt: String? = null
)
