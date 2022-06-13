package com.ioh_c22_h2_4.hy_ponics

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ioh_c22_h2_4.hy_ponics.util.PredictionResult

class SharedViewModel : ViewModel() {
    private val _uri = MutableLiveData(Uri.EMPTY)
    val uri: LiveData<Uri> get() = _uri

    private val _predictionResult = MutableLiveData(PredictionResult("", 0f))
    val predictionResult: LiveData<PredictionResult> get() = _predictionResult

    fun setPredictionResult(predictionResult: PredictionResult) {
        _predictionResult.postValue(predictionResult)
    }

    fun setUri(uri: Uri) {
        _uri.postValue(uri)
    }
}