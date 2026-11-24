package com.livesignalmap.mobile.domain.service

import com.livesignalmap.mobile.domain.model.SpeedTestResult

interface SpeedTestService {
    suspend fun measure(): SpeedTestResult
}