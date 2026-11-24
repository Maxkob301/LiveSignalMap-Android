
package com.livesignalmap.mobile.data.speedtest

import com.livesignalmap.mobile.domain.model.SpeedTestResult
import com.livesignalmap.mobile.domain.service.SpeedTestService
import kotlinx.coroutines.delay
import java.io.IOException

class FakeSpeedTestService(
    private val shouldFail: Boolean = false
) : SpeedTestService {

    override suspend fun measure(): SpeedTestResult {
        delay(5_000)

        if (shouldFail) {
            throw IOException("Simulated network error")
        }

        return SpeedTestResult(
            downloadMbps = 85.4,
            uploadMbps = 32.7,
            latencyMs = 18.0
        )
    }
}
