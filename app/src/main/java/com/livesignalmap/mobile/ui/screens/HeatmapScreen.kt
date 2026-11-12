
package com.livesignalmap.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.livesignalmap.mobile.data.model.FloorRegion
import com.livesignalmap.mobile.ui.components.InteractiveFloorPlan
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.livesignalmap.mobile.ui.components.NetworkModeSelector
import com.livesignalmap.mobile.ui.model.NetworkMode
import androidx.compose.ui.graphics.Color
import com.livesignalmap.mobile.data.model.MobileHeatmapDto
import com.livesignalmap.mobile.data.model.WifiHeatmapDto
import com.livesignalmap.mobile.ui.mapper.HeatmapColorMapper


@Composable
fun HeatmapScreen(
    modifier: Modifier = Modifier,
    heatmapViewModel: HeatmapViewModel = viewModel()
) {
    val uiState by heatmapViewModel.uiState.collectAsStateWithLifecycle()

    var selectedRegion by remember {
        mutableStateOf<FloorRegion?>(null)
    }
    var selectedNetworkMode by remember {
        mutableStateOf(NetworkMode.WIFI)
    }

    // Последнее выбранное помещение сохраняется
    // на время анимации закрытия.
    var displayedRegion by remember {
        mutableStateOf<FloorRegion?>(null)
    }


    val regionColors = remember(
        uiState.wifiData,
        uiState.mobileData,
        uiState.mapRegions,
        selectedNetworkMode
    ) {
        when (selectedNetworkMode) {
            NetworkMode.WIFI -> HeatmapColorMapper.forWifi(
                measurements = uiState.wifiData,
                regions = uiState.mapRegions
            )

            NetworkMode.MOBILE -> HeatmapColorMapper.forMobile(
                measurements = uiState.mobileData,
                regions = uiState.mapRegions
            )
        }
    }


    val selectedAuditoriumId = remember(
        displayedRegion?.id,
        uiState.mapRegions
    ) {
        uiState.mapRegions
            .firstOrNull { it.regionId == displayedRegion?.id }
            ?.auditoriumId
    }


    val selectedWifiData = remember(
        selectedAuditoriumId,
        uiState.wifiData
    ) {
        uiState.wifiData.firstOrNull {
            it.auditoriumId == selectedAuditoriumId
        }
    }

    val selectedMobileData = remember(
        selectedAuditoriumId,
        uiState.mobileData
    ) {
        uiState.mobileData.filter {
            it.auditoriumId == selectedAuditoriumId
        }
    }



    Box(
        modifier = modifier.fillMaxSize()
    ) {
        InteractiveFloorPlan(
            modifier = Modifier.fillMaxSize(),
            selectedRegionId = selectedRegion?.id,

            regionColors = regionColors,
            onRegionSelected = { region ->
                selectedRegion = region

                if (region != null) {
                    displayedRegion = region
                }
            }

        )

        NetworkModeSelector(
            selectedMode = selectedNetworkMode,
            onModeSelected = { mode ->
                selectedNetworkMode = mode
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )


        HeatmapStatusPanel(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onRetry = {
                heatmapViewModel.loadHeatmap()
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 76.dp
                )
        )


        AnimatedVisibility(
            visible = selectedRegion != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),

            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 220)
            ),

            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 220,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(durationMillis = 180)
            )
        ) {
            displayedRegion?.let { region ->

                AuditoriumInfoPanel(
                    region = region,
                    wifiData = selectedWifiData,
                    mobileData = selectedMobileData,
                    onClose = {
                        selectedRegion = null
                    },
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }
    }
}


@Composable
private fun AuditoriumInfoPanel(
    region: FloorRegion,
    wifiData: WifiHeatmapDto?,
    mobileData: List<MobileHeatmapDto>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ВЫБРАННОЕ ПОМЕЩЕНИЕ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = region.number?.let { "Помещение $it" }
                            ?: "Помещение без номера",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Первый этаж",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть"
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null
                        )
                        Text(
                            text = "Wi-Fi",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }


                    if (wifiData != null && wifiData.measurementCount > 0) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "↓ ${wifiData.avgDownloadSpeed ?: "—"} Мбит/с",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "↑ ${wifiData.avgUploadSpeed ?: "—"} Мбит/с",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "Ping: ${wifiData.avgPing ?: "—"} мс",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "Замеров: ${wifiData.measurementCount}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else {
                        Text(
                            text = "Нет измерений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null
                        )
                        Text(
                            text = "Мобильная сеть",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }


                    if (mobileData.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mobileData.forEach { measurement ->
                                Column {
                                    Text(
                                        text = measurement.operator,
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    Text(
                                        text = "↓ ${measurement.avgDownloadSpeed ?: "—"} Мбит/с",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "↑ ${measurement.avgUploadSpeed ?: "—"} Мбит/с",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "Ping: ${measurement.avgPing ?: "—"} мс",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Нет измерений",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            }
        }
    }
}


@Composable
private fun HeatmapStatusPanel(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isLoading && error == null) return

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )

                    Text(
                        text = "Загрузка измерений...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (error != null) {
                Text(
                    text = "Не удалось загрузить измерения",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Сервер недоступен. План этажа продолжает работать.",
                    style = MaterialTheme.typography.bodySmall
                )

                TextButton(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}

