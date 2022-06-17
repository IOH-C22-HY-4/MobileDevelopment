package com.ioh_c22_h2_4.hy_ponics

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivitySplashScreenBinding
import com.ioh_c22_h2_4.hy_ponics.helper.PrefHelper
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashScreenBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth

    private val pref by lazy { PrefHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (pref.getBoolean("pref_is_dark_mode")) {
            true -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            false -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        auth = FirebaseAuth.getInstance()

        lifecycleScope.launchWhenCreated {
            delay(2000)
            checkUser()
        }
    }

    private fun checkUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            Intent(this@SplashScreenActivity, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}