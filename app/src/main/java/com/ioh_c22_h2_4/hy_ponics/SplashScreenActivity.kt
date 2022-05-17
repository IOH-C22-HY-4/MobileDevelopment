package com.ioh_c22_h2_4.hy_ponics

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySplashScreenBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated {
            delay(2000)
            Intent(this@SplashScreenActivity, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}