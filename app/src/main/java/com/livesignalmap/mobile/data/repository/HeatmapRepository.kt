
package com.livesignalmap.mobile.data.repository

import com.livesignalmap.mobile.data.model.AuditoriumMapRegionDto
import com.livesignalmap.mobile.data.model.MobileHeatmapDto
import com.livesignalmap.mobile.data.model.WifiHeatmapDto
import com.livesignalmap.mobile.data.network.RetrofitClient

class HeatmapRepository {

    suspend fun getWifiHeatmap(): List<WifiHeatmapDto> {
        return RetrofitClient.apiService.getWifiHeatmap(
            from = "2026-01-01T00:00:00",
            to = "2026-12-31T23:59:59"
        )
    }

    suspend fun getMobileHeatmap(): List<MobileHeatmapDto> {
        return RetrofitClient.apiService.getMobileHeatmap(
            from = "2026-01-01T00:00:00",
            to = "2026-12-31T23:59:59"
        )
    }

    suspend fun getAuditoriumMapRegions(): List<AuditoriumMapRegionDto> {
        return RetrofitClient.apiService.getAuditoriumMapRegions()
    }
}
