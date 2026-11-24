package com.livesignalmap.mobile.ui.measurement

import com.livesignalmap.mobile.domain.model.SpeedTestResult

sealed interface SpeedTestState {

    data object Idle : SpeedTestState

    data object Running : SpeedTestState

    data class Success(
        val result: SpeedTestResult
    ) : SpeedTestState

    data class Error(
        val message: String
    ) : SpeedTestState
}
