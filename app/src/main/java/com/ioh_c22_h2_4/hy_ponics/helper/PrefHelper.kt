package com.ioh_c22_h2_4.hy_ponics.helper

import android.content.Context
import android.content.SharedPreferences

class PrefHelper (context: Context) {

    private val PREFS_NAME = "sharedpref12345"
    private var sharedPref: SharedPreferences
    val editor: SharedPreferences.Editor

    init {
        sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    fun put(key: String, value: Boolean) {
        editor.putBoolean(key, value)
            .apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }
}