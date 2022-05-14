package com.ioh_c22_h2_4.hy_ponics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}