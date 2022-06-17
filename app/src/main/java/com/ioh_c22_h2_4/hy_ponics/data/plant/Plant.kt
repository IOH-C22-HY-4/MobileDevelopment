package com.ioh_c22_h2_4.hy_ponics.data.plant

data class Plant(
    val ECmax: Double = 0.0,
    val ECmin: Double = 0.0,
    val TDSmax: Int = 0,
    val TDSmin: Int = 0,
    val ecImage: String = "",
    val pHmax: Double = 0.0,
    val pHmin: Double = 0.0,
    val phImage: String = "",
    val tdsImage: String = "",
)
