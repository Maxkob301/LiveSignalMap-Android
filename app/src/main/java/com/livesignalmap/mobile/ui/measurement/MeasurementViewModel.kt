
package com.livesignalmap.mobile.ui.measurement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.livesignalmap.mobile.data.local.entity.MeasurementEntity
import com.livesignalmap.mobile.data.repository.MeasurementRepository
import com.livesignalmap.mobile.domain.service.SpeedTestService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ensureActive


class MeasurementViewModel(
    private val repository: MeasurementRepository,
    private val speedTestService: SpeedTestService
) : ViewModel() {

    private var measurementJob: Job? = null

    private val _speedTestState =
        MutableStateFlow<SpeedTestState>(SpeedTestState.Idle)

    val speedTestState: StateFlow<SpeedTestState> =
        _speedTestState.asStateFlow()

    val measurements: StateFlow<List<MeasurementEntity>> =
        repository.measurements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun startMeasurement() {
        if (measurementJob?.isCompleted == false) return

        _speedTestState.value = SpeedTestState.Running

        measurementJob = viewModelScope.launch(
            start = CoroutineStart.UNDISPATCHED
        ) {
            try {
                val result = speedTestService.measure()

                ensureActive()

                _speedTestState.value = SpeedTestState.Success(result)

            } catch (e: CancellationException) {
                _speedTestState.value = SpeedTestState.Idle
                throw e

            } catch (e: Exception) {
                _speedTestState.value = SpeedTestState.Error(
                    message = "Не удалось выполнить измерение"
                )
            }
        }
    }

    fun cancelMeasurement() {
        measurementJob?.cancel()
    }

    companion object {

        fun factory(
            repository: MeasurementRepository,
            speedTestService: SpeedTestService
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            MeasurementViewModel::class.java
                        )
                    )

                    return MeasurementViewModel(
                        repository,
                        speedTestService
                    ) as T
                }
            }
        }
    }
}
