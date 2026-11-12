
package com.livesignalmap.mobile.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livesignalmap.mobile.data.repository.HeatmapRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import android.util.Log

class HeatmapViewModel : ViewModel() {

    private val repository = HeatmapRepository()

    private val _uiState = MutableStateFlow(
        HeatmapUiState(isLoading = true)
    )

    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()

    init {
        loadHeatmap()
    }

    fun loadHeatmap() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            supervisorScope {

                val regionsDeferred = async {
                    try {
                        repository.getAuditoriumMapRegions()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val wifiDeferred = async {
                    try {
                        repository.getWifiHeatmap()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val mobileDeferred = async {
                    try {
                        repository.getMobileHeatmap()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                val regions = regionsDeferred.await()
                Log.d("LiveSignalMap", "Загружено соответствий: ${regions.size}")
                val wifi = wifiDeferred.await()
                val mobile = mobileDeferred.await()

                _uiState.value = HeatmapUiState(
                    isLoading = false,
                    mapRegions = regions,
                    wifiData = wifi,
                    mobileData = mobile
                )
            }
        }
    }
}
