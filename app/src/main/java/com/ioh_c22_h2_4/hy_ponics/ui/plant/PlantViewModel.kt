package com.ioh_c22_h2_4.hy_ponics.ui.plant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.ioh_c22_h2_4.hy_ponics.data.plant.Plant
import com.ioh_c22_h2_4.hy_ponics.data.repository.plant.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val plantRepository: PlantRepository
) : ViewModel() {

    private val _query = MutableLiveData("")

    val bestParameter: LiveData<Plant> = _query.switchMap {
        plantRepository.getBestParameterBy(it).asLiveData(viewModelScope.coroutineContext)
    }

    fun getBestParameter(query: String) {
        _query.value = query
    }

}
