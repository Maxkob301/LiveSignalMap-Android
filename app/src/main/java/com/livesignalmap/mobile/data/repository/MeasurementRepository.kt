
package com.livesignalmap.mobile.data.repository

import com.livesignalmap.mobile.data.local.dao.MeasurementDao
import com.livesignalmap.mobile.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(
    private val measurementDao: MeasurementDao
) {

    val measurements: Flow<List<MeasurementEntity>> =
        measurementDao.observeAll()

    suspend fun saveMeasurement(
        measurement: MeasurementEntity
    ) {
        measurementDao.insert(measurement)
    }

    suspend fun getPendingMeasurements(): List<MeasurementEntity> {
        return measurementDao.getPending()
    }

    suspend fun getMeasurementById(
        id: String
    ): MeasurementEntity? {
        return measurementDao.getById(id)
    }

    suspend fun updateSyncStatus(
        id: String,
        status: String
    ) {
        measurementDao.updateSyncStatus(id, status)
    }
}
