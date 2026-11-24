
package com.livesignalmap.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.livesignalmap.mobile.ui.measurement.MeasurementViewModel
import com.livesignalmap.mobile.ui.measurement.SpeedTestState

@Composable
fun MeasurementScreen(
    viewModel: MeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.measurements
        .collectAsStateWithLifecycle()

    val speedTestState by viewModel.speedTestState
        .collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            Alignment.CenterVertically
        )
    ) {
        Text(
            text = "Измерение скорости",
            style = MaterialTheme.typography.headlineMedium
        )

        when (val state = speedTestState) {

            SpeedTestState.Idle -> {
                Button(
                    onClick = viewModel::startMeasurement
                ) {
                    Text("Начать измерение")
                }
            }

            SpeedTestState.Running -> {
                CircularProgressIndicator()

                Text("Выполняется измерение...")

                OutlinedButton(
                    onClick = viewModel::cancelMeasurement
                ) {
                    Text("Отменить")
                }
            }

            is SpeedTestState.Success -> {
                Text(
                    text = "Загрузка: ${state.result.downloadMbps} Мбит/с"
                )

                Text(
                    text = "Отдача: ${state.result.uploadMbps} Мбит/с"
                )

                Text(
                    text = state.result.latencyMs?.let {
                        "Задержка: $it мс"
                    } ?: "Задержка недоступна"
                )

                Button(
                    onClick = viewModel::startMeasurement
                ) {
                    Text("Повторить измерение")
                }
            }

            is SpeedTestState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = viewModel::startMeasurement
                ) {
                    Text("Повторить измерение")
                }
            }
        }

        Text(
            text = if (measurements.isEmpty()) {
                "История измерений пока пуста"
            } else {
                "Сохранено измерений: ${measurements.size}"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
