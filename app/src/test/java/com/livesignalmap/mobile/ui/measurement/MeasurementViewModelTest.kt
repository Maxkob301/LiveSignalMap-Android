
package com.livesignalmap.mobile.ui.measurement

import androidx.lifecycle.viewModelScope
import com.livesignalmap.mobile.data.local.dao.FakeMeasurementDao
import com.livesignalmap.mobile.data.repository.MeasurementRepository
import com.livesignalmap.mobile.data.speedtest.FakeSpeedTestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.livesignalmap.mobile.domain.model.SpeedTestResult
import com.livesignalmap.mobile.domain.service.SpeedTestService

@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: MeasurementRepository
    private lateinit var viewModel: MeasurementViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        repository = MeasurementRepository(
            FakeMeasurementDao()
        )

        viewModel = MeasurementViewModel(
            repository = repository,
            speedTestService = FakeSpeedTestService()
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsIdle() {
        assertEquals(
            SpeedTestState.Idle,
            viewModel.speedTestState.value
        )
    }

    @Test
    fun startMeasurementChangesStateToRunning() = runTest {
        viewModel.startMeasurement()

        assertEquals(
            SpeedTestState.Running,
            viewModel.speedTestState.value
        )
    }

    @Test
    fun successfulMeasurementReturnsCorrectResult() = runTest {
        viewModel.startMeasurement()

        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()

        val state = viewModel.speedTestState.value

        assertTrue(state is SpeedTestState.Success)

        val result = (state as SpeedTestState.Success).result

        assertEquals(85.4, result.downloadMbps, 0.001)
        assertEquals(32.7, result.uploadMbps, 0.001)
        assertEquals(18.0, requireNotNull(result.latencyMs), 0.001)
    }

    @Test
    fun failedMeasurementChangesStateToError() = runTest {
        viewModel.viewModelScope.cancel()

        viewModel = MeasurementViewModel(
            repository = repository,
            speedTestService = FakeSpeedTestService(
                shouldFail = true
            )
        )

        viewModel.startMeasurement()

        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()

        val state = viewModel.speedTestState.value

        assertTrue(state is SpeedTestState.Error)

        assertEquals(
            "Не удалось выполнить измерение",
            (state as SpeedTestState.Error).message
        )
    }

    @Test
    fun cancelledMeasurementReturnsToIdle() = runTest {
        viewModel.startMeasurement()

        runCurrent()

        viewModel.cancelMeasurement()

        runCurrent()

        assertEquals(
            SpeedTestState.Idle,
            viewModel.speedTestState.value
        )
    }

    @Test
    fun immediateCancellationReturnsToIdle() = runTest {
        viewModel.startMeasurement()

        viewModel.cancelMeasurement()

        runCurrent()

        assertEquals(
            SpeedTestState.Idle,
            viewModel.speedTestState.value
        )
    }

    private class CountingSpeedTestService : SpeedTestService {

        private val delegate = FakeSpeedTestService()

        var callCount = 0
            private set

        override suspend fun measure(): SpeedTestResult {
            callCount++
            return delegate.measure()
        }
    }

    @Test
    fun duplicateStartDoesNotLaunchSecondMeasurement() = runTest {
        viewModel.viewModelScope.cancel()

        val countingService = CountingSpeedTestService()

        viewModel = MeasurementViewModel(
            repository = repository,
            speedTestService = countingService
        )

        viewModel.startMeasurement()
        viewModel.startMeasurement()

        runCurrent()

        assertEquals(1, countingService.callCount)

        assertEquals(
            SpeedTestState.Running,
            viewModel.speedTestState.value
        )

        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(
            viewModel.speedTestState.value is SpeedTestState.Success
        )

        assertEquals(1, countingService.callCount)
    }
    @Test
    fun measurementCanBeRestartedAfterCompletion() = runTest {
        viewModel.viewModelScope.cancel()

        val countingService = CountingSpeedTestService()

        viewModel = MeasurementViewModel(
            repository = repository,
            speedTestService = countingService
        )

        viewModel.startMeasurement()

        runCurrent()
        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(
            viewModel.speedTestState.value is SpeedTestState.Success
        )

        assertEquals(1, countingService.callCount)

        viewModel.startMeasurement()

        runCurrent()

        assertEquals(
            SpeedTestState.Running,
            viewModel.speedTestState.value
        )

        assertEquals(2, countingService.callCount)

        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(
            viewModel.speedTestState.value is SpeedTestState.Success
        )

        assertEquals(2, countingService.callCount)
    }

    @Test
    fun restartIsBlockedUntilCancellationCompletes() = runTest {
        viewModel.viewModelScope.cancel()

        val countingService = CountingSpeedTestService()

        viewModel = MeasurementViewModel(
            repository = repository,
            speedTestService = countingService
        )

        viewModel.startMeasurement()
        runCurrent()

        assertEquals(1, countingService.callCount)

        viewModel.cancelMeasurement()
        viewModel.startMeasurement()

        assertEquals(1, countingService.callCount)

        runCurrent()

        assertEquals(
            SpeedTestState.Idle,
            viewModel.speedTestState.value
        )

        viewModel.startMeasurement()
        runCurrent()

        assertEquals(2, countingService.callCount)

        assertEquals(
            SpeedTestState.Running,
            viewModel.speedTestState.value
        )

        advanceTimeBy(5_000)
        runCurrent()

        assertTrue(
            viewModel.speedTestState.value is SpeedTestState.Success
        )
    }
}
