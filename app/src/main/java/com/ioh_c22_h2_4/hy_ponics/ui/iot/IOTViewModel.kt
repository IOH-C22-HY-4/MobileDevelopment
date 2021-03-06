package com.ioh_c22_h2_4.hy_ponics.ui.iot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ioh_c22_h2_4.hy_ponics.data.repository.iot.IOTRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IOTViewModel @Inject constructor(
    private val iotRepository: IOTRepository
) : ViewModel() {

    val sensorData = iotRepository.getSensorData().asLiveData(viewModelScope.coroutineContext)

}