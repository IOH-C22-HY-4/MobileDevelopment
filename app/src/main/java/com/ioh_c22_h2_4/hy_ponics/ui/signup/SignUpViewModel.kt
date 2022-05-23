package com.ioh_c22_h2_4.hy_ponics.ui.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ioh_c22_h2_4.hy_ponics.data.remote.ApiConfig
import com.ioh_c22_h2_4.hy_ponics.data.remote.FileUploadResponse
import com.ioh_c22_h2_4.hy_ponics.util.ApiCallbackString
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(name: String, email: String, pass: String, callback: ApiCallbackString){
        _isLoading.value = true

        val service = ApiConfig().getApiService().signup(name, email, pass)
        service.enqueue(object : Callback<FileUploadResponse> {
            override fun onResponse(
                call: Call<FileUploadResponse>,
                response: Response<FileUploadResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error)
                        callback.onResponse(response.body() != null, SUCCESS)

                } else {
                    Log.e(TAG, "onFailure1: ${response.message()}")

                    // get message error
                    val jsonObject = JSONTokener(response.errorBody()!!.string()).nextValue() as JSONObject
                    val message = jsonObject.getString("message")
                    callback.onResponse(false, message)
                }
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure2: ${t.message}")
                callback.onResponse(false, t.message.toString())
            }
        })
    }

    companion object {
        private const val TAG = "SignUpViewModel"
        private const val SUCCESS = "success"
    }
}

