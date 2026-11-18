package com.livesignalmap.mobile

import android.app.Application
import androidx.room.Room
import com.livesignalmap.mobile.data.local.AppDatabase
import com.livesignalmap.mobile.data.repository.MeasurementRepository

class LiveSignalMapApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "livesignalmap.db"
        ).build()
    }
    val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepository(database.measurementDao())
    }
}
