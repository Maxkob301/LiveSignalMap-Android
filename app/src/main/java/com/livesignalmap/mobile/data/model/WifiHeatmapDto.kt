package com.livesignalmap.mobile.data.model

import com.google.gson.annotations.SerializedName

data class WifiHeatmapDto(
    @SerializedName("auditoriumId") val auditoriumId: Long,
    @SerializedName("auditoriumName") val auditoriumName: String,
    @SerializedName("building") val building: String,
    @SerializedName("avgDownloadSpeed") val avgDownloadSpeed: Double?,
    @SerializedName("avgUploadSpeed") val avgUploadSpeed: Double?,
    @SerializedName("avgPing") val avgPing: Double?,
    @SerializedName("measurementCount") val measurementCount: Long
)