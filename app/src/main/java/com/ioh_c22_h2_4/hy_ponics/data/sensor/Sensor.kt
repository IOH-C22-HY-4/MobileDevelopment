package com.ioh_c22_h2_4.hy_ponics.data.sensor

data class Sensor(
    val ecSensor: SensorData = SensorData(),
    val phSensor: SensorData = SensorData(),
    val tdsSensor: SensorData = SensorData()
)
