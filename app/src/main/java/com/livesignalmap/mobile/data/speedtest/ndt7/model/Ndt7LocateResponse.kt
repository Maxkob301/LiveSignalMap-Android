package com.livesignalmap.mobile.data.speedtest.ndt7.model

import com.google.gson.annotations.SerializedName

internal data class Ndt7LocateResponse(
    @SerializedName("results")
    val results: List<Ndt7LocateTarget?>?
)

internal data class Ndt7LocateTarget(
    @SerializedName("machine")
    val machine: String?,

    @SerializedName("urls")
    val urls: Map<String, String?>?
)