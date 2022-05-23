package com.ioh_c22_h2_4.hy_ponics.ui.check

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.ioh_c22_h2_4.hy_ponics.R
import com.ioh_c22_h2_4.hy_ponics.data.helper.UserPreference
import com.ioh_c22_h2_4.hy_ponics.ui.ViewModelFactory
import com.ioh_c22_h2_4.hy_ponics.ui.login.LoginActivity
import com.ioh_c22_h2_4.hy_ponics.ui.main.MainActivity
import com.ioh_c22_h2_4.hy_ponics.ui.main.MainViewModel

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class CheckActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        setupViewModel()
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) {
            if (it.isLogin) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}