package com.livesignalmap.mobile.data.network

import com.livesignalmap.mobile.data.model.AuditoriumMapRegionDto
import com.livesignalmap.mobile.data.model.MobileHeatmapDto
import com.livesignalmap.mobile.data.model.WifiHeatmapDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/heatmap/wifi")
    suspend fun getWifiHeatmap(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<WifiHeatmapDto>


    @GET("api/heatmap/mobile")
    suspend fun getMobileHeatmap(
        @Query("from") from: String,
        @Query("to") to: String
    ): List<MobileHeatmapDto>

    @GET("api/auditoriums/map-regions")
    suspend fun getAuditoriumMapRegions(): List<AuditoriumMapRegionDto>
}