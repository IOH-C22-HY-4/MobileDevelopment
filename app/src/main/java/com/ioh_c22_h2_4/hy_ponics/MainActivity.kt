package com.ioh_c22_h2_4.hy_ponics

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ioh_c22_h2_4.hy_ponics.databinding.ActivityMainBinding
import com.ioh_c22_h2_4.hy_ponics.helper.PrefHelper
import com.ioh_c22_h2_4.hy_ponics.util.Constants.NOTIFICATION_CHANNEL
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment }
    private val navController by lazy { navHostFragment.findNavController() }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bottomNav.setupWithNavController(navController)

        val workManager = WorkManager.getInstance(this.applicationContext)

        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresDeviceIdle(false)
            .setRequiresBatteryNotLow(false)
            .setTriggerContentMaxDelay(15L, TimeUnit.MINUTES)
            .build()

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>(
        ).setConstraints(constraint)
            .setInitialDelay(15L, TimeUnit.MINUTES)
            .addTag(NOTIFICATION_CHANNEL)
            .build()


        workManager.enqueue(notificationWorkRequest)
    }
}