package com.ioh_c22_h2_4.hy_ponics.data.plant

data class Plant(
    val ECmax: Double,
    val ECmin: Double,
    val TDSmax: Int,
    val TDSmin: Int,
    val ecImage: String,
    val pHmax: Double,
    val pHMin: Double,
    val phImage: String,
    val tdsImage: String,
)
