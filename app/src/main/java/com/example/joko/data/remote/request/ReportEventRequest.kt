package com.example.joko.data.remote.request

import com.example.joko.utils.ReportType
import com.google.gson.annotations.SerializedName

data class ReportEventRequest (
    @SerializedName("event_id") val eventId: String,
    @SerializedName("reporter_id") val reporterId: String?,
    @SerializedName("report_category") val category: ReportType,
)
