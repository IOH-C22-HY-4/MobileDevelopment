package com.ioh_c22_h2_4.hy_ponics.data.repository

import com.ioh_c22_h2_4.hy_ponics.data.sensor.Sensor
import kotlinx.coroutines.flow.Flow

interface IOTRepository {
    fun getSensorData(): Flow<Sensor>
}