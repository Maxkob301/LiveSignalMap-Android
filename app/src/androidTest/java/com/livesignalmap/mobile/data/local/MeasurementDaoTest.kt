package com.livesignalmap.mobile.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.livesignalmap.mobile.data.local.dao.MeasurementDao
import com.livesignalmap.mobile.data.local.entity.MeasurementEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeasurementDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: MeasurementDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        dao = database.measurementDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadMeasurement() = runBlocking {
        val measurement = MeasurementEntity(
            id = "test-001",
            networkType = "WIFI",
            downloadSpeed = 85.5,
            uploadSpeed = 32.4,
            ping = 15,
            bssid = "00:11:22:33:44:55",
            rssi = -55,
            operator = null,
            auditoriumId = 101L,
            createdAt = 1000000L,
            syncStatus = "PENDING"
        )

        dao.insert(measurement)

        val saved = dao.getById("test-001")

        assertNotNull(saved)
        assertEquals(measurement, saved)
    }

    @Test
    fun updateMeasurementSyncStatus() = runBlocking {
        val measurement = MeasurementEntity(
            id = "test-002",
            networkType = "MOBILE",
            downloadSpeed = 45.0,
            uploadSpeed = 12.0,
            ping = 30,
            bssid = null,
            rssi = null,
            operator = "TestOperator",
            auditoriumId = 102L,
            createdAt = 2000000L,
            syncStatus = "PENDING"
        )

        dao.insert(measurement)

        dao.updateSyncStatus(
            id = "test-002",
            status = "SYNCED"
        )

        val updated = dao.getById("test-002")

        assertEquals("SYNCED", updated?.syncStatus)
    }

    @Test
    fun getPendingReturnsOnlyPendingMeasurements() = runBlocking {
        val pending = MeasurementEntity(
            id = "test-003",
            networkType = "WIFI",
            downloadSpeed = 60.0,
            uploadSpeed = 20.0,
            ping = 18,
            bssid = null,
            rssi = -60,
            operator = null,
            auditoriumId = 103L,
            createdAt = 3000000L,
            syncStatus = "PENDING"
        )

        val synced = pending.copy(
            id = "test-004",
            createdAt = 4000000L,
            syncStatus = "SYNCED"
        )

        dao.insert(pending)
        dao.insert(synced)

        val results = dao.getPending()

        assertEquals(1, results.size)
        assertEquals("test-003", results.first().id)
    }
}
