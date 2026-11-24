package com.livesignalmap.mobile.domain.model

data class SpeedTestResult(
    val downloadMbps: Double,
    val uploadMbps: Double,
    val latencyMs: Double?
)
