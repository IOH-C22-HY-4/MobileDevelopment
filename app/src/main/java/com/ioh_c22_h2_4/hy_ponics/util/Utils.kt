package com.ioh_c22_h2_4.hy_ponics.util

import android.util.Patterns

fun isEmailValid(email: CharSequence): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

interface ApiCallbackString {
    fun onResponse(success: Boolean, message: String)
}