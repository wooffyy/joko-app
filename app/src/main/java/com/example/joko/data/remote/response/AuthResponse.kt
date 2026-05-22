package com.example.joko.data.remote.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    // Case 1: Tokens are at top level
    @SerializedName("access_token") val accessTokenTop: String? = null,
    @SerializedName("refresh_token") val refreshTokenTop: String? = null,
    
    // Case 2: Tokens are inside a session object (Common in newer Supabase)
    @SerializedName("session") val session: SessionResponse? = null,
    
    @SerializedName("user") val user: UserResponse? = null,
    
    // Case 3: Registration with email confirmation might return user fields at top level
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null
) {
    val accessToken: String? get() = accessTokenTop ?: session?.accessToken
    val refreshToken: String? get() = refreshTokenTop ?: session?.refreshToken
    val userId: String? get() = user?.id ?: session?.user?.id ?: id
}

data class SessionResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user") val user: UserResponse? = null
)

data class UserResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerializedName("last_sign_in_at") val lastSignInAt: String? = null
)
