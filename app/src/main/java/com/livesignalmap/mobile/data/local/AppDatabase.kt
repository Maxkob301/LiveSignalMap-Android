package com.livesignalmap.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.livesignalmap.mobile.data.local.dao.MeasurementDao
import com.livesignalmap.mobile.data.local.entity.MeasurementEntity

@Database(
    entities = [MeasurementEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun measurementDao(): MeasurementDao
}
