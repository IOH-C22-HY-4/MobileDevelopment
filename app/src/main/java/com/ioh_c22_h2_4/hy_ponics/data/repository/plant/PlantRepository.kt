package com.ioh_c22_h2_4.hy_ponics.data.repository.plant

import com.ioh_c22_h2_4.hy_ponics.data.plant.Plant
import kotlinx.coroutines.flow.Flow

interface PlantRepository {
    fun getBestParameterBy(query: String): Flow<Plant>
}