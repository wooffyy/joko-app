package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: Map<String, Any?>? = null
)