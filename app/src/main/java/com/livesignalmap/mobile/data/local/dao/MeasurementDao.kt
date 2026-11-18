
package com.livesignalmap.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.livesignalmap.mobile.data.local.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(measurement: MeasurementEntity)

    @Query("""
        SELECT * FROM measurements
        ORDER BY createdAt DESC
    """)
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("""
        SELECT * FROM measurements
        WHERE syncStatus = 'PENDING'
        ORDER BY createdAt ASC
    """)
    suspend fun getPending(): List<MeasurementEntity>

    @Query("""
        UPDATE measurements
        SET syncStatus = :status
        WHERE id = :id
    """)
    suspend fun updateSyncStatus(
        id: String,
        status: String
    )

    @Query("""
        SELECT * FROM measurements
        WHERE id = :id
        LIMIT 1
    """)
    suspend fun getById(id: String): MeasurementEntity?
}
