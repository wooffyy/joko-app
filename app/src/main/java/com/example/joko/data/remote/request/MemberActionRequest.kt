package com.example.joko.data.remote.request

import com.google.gson.annotations.SerializedName

data class MemberActionRequest(
    @SerializedName("status") val status: String // accepted, rejected
)
