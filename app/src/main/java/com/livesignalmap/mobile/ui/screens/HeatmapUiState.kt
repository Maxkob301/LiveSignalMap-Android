
package com.livesignalmap.mobile.ui.screens

import com.livesignalmap.mobile.data.model.AuditoriumMapRegionDto
import com.livesignalmap.mobile.data.model.MobileHeatmapDto
import com.livesignalmap.mobile.data.model.WifiHeatmapDto

data class HeatmapUiState(
    val isLoading: Boolean = false,
    val wifiData: List<WifiHeatmapDto> = emptyList(),
    val mobileData: List<MobileHeatmapDto> = emptyList(),
    val mapRegions: List<AuditoriumMapRegionDto> = emptyList(),
    val error: String? = null
)
