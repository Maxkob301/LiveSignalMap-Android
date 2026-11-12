
package com.livesignalmap.mobile.ui.mapper

import androidx.compose.ui.graphics.Color
import com.livesignalmap.mobile.data.model.AuditoriumMapRegionDto
import com.livesignalmap.mobile.data.model.MobileHeatmapDto
import com.livesignalmap.mobile.data.model.WifiHeatmapDto

object HeatmapColorMapper {

    private val goodColor = Color(0xFF4CAF50)
    private val mediumColor = Color(0xFFFFC107)
    private val poorColor = Color(0xFFF44336)

    private fun colorForSpeed(speed: Double): Color {
        return when {
            speed >= 50.0 -> goodColor
            speed >= 20.0 -> mediumColor
            else -> poorColor
        }
    }

    fun forWifi(
        measurements: List<WifiHeatmapDto>,
        regions: List<AuditoriumMapRegionDto>
    ): Map<String, Color> {

        val speedsByAuditorium = measurements
            .filter {
                it.measurementCount > 0 &&
                        it.avgDownloadSpeed != null &&
                        it.avgDownloadSpeed.isFinite() &&
                        it.avgDownloadSpeed >= 0.0
            }
            .groupBy { it.auditoriumId }
            .mapValues { (_, values) ->
                values.sumOf {
                    it.avgDownloadSpeed!! * it.measurementCount
                } / values.sumOf {
                    it.measurementCount
                }
            }

        return regions.mapNotNull { region ->
            val speed = speedsByAuditorium[region.auditoriumId]
                ?: return@mapNotNull null

            region.regionId to colorForSpeed(speed)
        }.toMap()
    }

    fun forMobile(
        measurements: List<MobileHeatmapDto>,
        regions: List<AuditoriumMapRegionDto>
    ): Map<String, Color> {

        val speedsByAuditorium = measurements
            .filter {
                it.measurementCount > 0 &&
                        it.avgDownloadSpeed != null &&
                        it.avgDownloadSpeed.isFinite() &&
                        it.avgDownloadSpeed >= 0.0
            }
            .groupBy { it.auditoriumId }
            .mapValues { (_, values) ->
                values.sumOf {
                    it.avgDownloadSpeed!! * it.measurementCount
                } / values.sumOf {
                    it.measurementCount
                }
            }

        return regions.mapNotNull { region ->
            val speed = speedsByAuditorium[region.auditoriumId]
                ?: return@mapNotNull null

            region.regionId to colorForSpeed(speed)
        }.toMap()
    }
}
