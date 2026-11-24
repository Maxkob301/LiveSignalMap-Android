
package com.livesignalmap.mobile.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livesignalmap.mobile.LiveSignalMapApplication
import com.livesignalmap.mobile.ui.measurement.MeasurementViewModel

private enum class MainTab {
    MAP,
    MEASUREMENTS
}

@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable {
        mutableStateOf(MainTab.MAP)
    }

    val stateHolder = rememberSaveableStateHolder()

    val application = LocalContext.current.applicationContext
            as LiveSignalMapApplication

    val measurementViewModel: MeasurementViewModel = viewModel(
        factory = MeasurementViewModel.factory(
            repository = application.measurementRepository,
            speedTestService = application.speedTestService
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.MAP,
                    onClick = {
                        selectedTab = MainTab.MAP
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Карта")
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.MEASUREMENTS,
                    onClick = {
                        selectedTab = MainTab.MEASUREMENTS
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text("Измерения")
                    }
                )
            }
        }
    ) { innerPadding ->

        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        stateHolder.SaveableStateProvider(
            key = selectedTab.name
        ) {
            when (selectedTab) {
                MainTab.MAP -> {
                    HeatmapScreen(
                        modifier = screenModifier
                    )
                }

                MainTab.MEASUREMENTS -> {
                    MeasurementScreen(
                        viewModel = measurementViewModel,
                        modifier = screenModifier
                    )
                }
            }
        }
    }
}
