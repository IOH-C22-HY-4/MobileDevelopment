package com.ioh_c22_h2_4.hy_ponics.data.repository.iot

import com.ioh_c22_h2_4.hy_ponics.data.sensor.SensorData
import kotlinx.coroutines.flow.Flow

interface IOTRepository {
    fun getSensorData(): Flow<List<SensorData>>
}