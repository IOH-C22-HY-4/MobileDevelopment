package com.ioh_c22_h2_4.hy_ponics

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _uri = MutableLiveData(Uri.EMPTY)
    val uri: LiveData<Uri> get() = _uri

    fun setUri(uri: Uri) {
        _uri.postValue(uri)
    }
}