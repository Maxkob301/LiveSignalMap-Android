
package com.livesignalmap.mobile.data.local.dao

import com.livesignalmap.mobile.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMeasurementDao : MeasurementDao {

    private val measurements =
        MutableStateFlow<List<MeasurementEntity>>(emptyList())

    override suspend fun insert(
        measurement: MeasurementEntity
    ) {
        measurements.update { current ->
            check(current.none { it.id == measurement.id })

            current + measurement
        }
    }

    override fun observeAll(): Flow<List<MeasurementEntity>> {
        return measurements.map { current ->
            current.sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getPending(): List<MeasurementEntity> {
        return measurements.value
            .filter { it.syncStatus == "PENDING" }
            .sortedBy { it.createdAt }
    }

    override suspend fun updateSyncStatus(
        id: String,
        status: String
    ) {
        measurements.update { current ->
            current.map { measurement ->
                if (measurement.id == id) {
                    measurement.copy(syncStatus = status)
                } else {
                    measurement
                }
            }
        }
    }

    override suspend fun getById(
        id: String
    ): MeasurementEntity? {
        return measurements.value.firstOrNull {
            it.id == id
        }
    }
}
