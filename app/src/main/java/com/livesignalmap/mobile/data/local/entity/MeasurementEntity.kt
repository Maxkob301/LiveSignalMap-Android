package com.livesignalmap.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey
    val id: String,
    val networkType: String,
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val ping: Int,
    val bssid: String?,
    val rssi: Int?,
    val operator: String?,
    val auditoriumId: Long?,
    val createdAt: Long,
    val syncStatus: String
)