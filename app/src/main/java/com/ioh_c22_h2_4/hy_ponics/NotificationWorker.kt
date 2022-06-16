package com.ioh_c22_h2_4.hy_ponics

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ioh_c22_h2_4.hy_ponics.data.repository.IOTRepository
import com.ioh_c22_h2_4.hy_ponics.data.sensor.SensorData
import com.ioh_c22_h2_4.hy_ponics.util.Constants.NOTIFICATION_CHANNEL
import com.ioh_c22_h2_4.hy_ponics.util.Constants.NOTIFICATION_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted private val params: WorkerParameters,
    private val iotRepository: IOTRepository
) : CoroutineWorker(ctx, params) {

    private fun getPendingIntent(sensorData: SensorData): PendingIntent? {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(NOTIFICATION_ID, sensorData.name)
        }

        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
        }
    }

    private fun createNotificationChannel(sensorData: SensorData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val descriptionText = "${sensorData.name} melebihi batas aman"
            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_CHANNEL, importance).apply {
                    description = descriptionText
                }

            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {

        iotRepository.getSensorData().collect {
            val isEcSafe = it[0].data > 1.5 && it[0].data < 1.8
            val isPhSafe = it[1].data > 6.5 && it[1].data < 7
            val isTdsSafe = it[2].data > 560 && it[2].data < 840

            val className = this@NotificationWorker.javaClass.simpleName

            delay(900000)

            Log.d(className, "$isEcSafe")
            Log.d(className, "$isPhSafe")
            Log.d(className, "$isTdsSafe")

            val notification = NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL
            ).setSmallIcon(R.drawable.splash_screen_image)

            Log.d(className, "$notification")

            if (!isEcSafe) {
                val ec = it[0]
                val pendingIntent = getPendingIntent(ec)
                notification.apply {
                    setContentTitle(ec.name)
                    setContentText("${ec.name} melebih batas aman")
                    setContentIntent(pendingIntent)
                    setChannelId(NOTIFICATION_CHANNEL)
                }
                Log.d(className, "$ec")
                createNotificationChannel(ec)
                with(NotificationManagerCompat.from(ctx)) {
                    notify(ec.data.toInt(), notification.build())
                }
            }
            if (!isPhSafe) {
                val ph = it[1]
                val pendingIntent = getPendingIntent(ph)
                notification.apply {
                    setContentTitle(ph.name)
                    setContentText("${ph.name} melebih batas aman")
                    setContentIntent(pendingIntent)
                    setChannelId(NOTIFICATION_CHANNEL)
                }
                Log.d(className, "$ph")
                createNotificationChannel(ph)
                with(NotificationManagerCompat.from(ctx)) {
                    notify(ph.data.toInt(), notification.build())
                }
            }
            if (!isTdsSafe) {
                val tds = it[1]
                val pendingIntent = getPendingIntent(tds)
                notification.apply {
                    setContentTitle(tds.name)
                    setContentText("${tds.name} melebih batas aman")
                    setContentIntent(pendingIntent)
                    setChannelId(NOTIFICATION_CHANNEL)
                }
                Log.d(className, "$tds")
                createNotificationChannel(tds)
                with(NotificationManagerCompat.from(ctx)) {
                    notify(tds.data.toInt(), notification.build())
                }
            }
        }

        return Result.success()
    }
}