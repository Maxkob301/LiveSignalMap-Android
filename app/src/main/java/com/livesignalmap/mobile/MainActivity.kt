
package com.livesignalmap.mobile

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.view.doOnPreDraw
import com.livesignalmap.mobile.ui.screens.HeatmapScreen
import com.livesignalmap.mobile.ui.theme.LiveSignalMapTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val startTime = SystemClock.elapsedRealtime()

        Log.d("StartupPerformance", "onCreate начался")

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        Log.d(
            "StartupPerformance",
            "Перед setContent: ${SystemClock.elapsedRealtime() - startTime} мс"
        )

        window.decorView.doOnPreDraw {
            Log.d(
                "StartupPerformance",
                "Первый pre-draw: ${SystemClock.elapsedRealtime() - startTime} мс"
            )
        }

        setContent {
            LiveSignalMapTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    HeatmapScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            SideEffect {
                Log.d(
                    "StartupPerformance",
                    "Compose SideEffect: ${SystemClock.elapsedRealtime() - startTime} мс"
                )
            }
        }

        Log.d(
            "StartupPerformance",
            "После setContent: ${SystemClock.elapsedRealtime() - startTime} мс"
        )
    }
}
