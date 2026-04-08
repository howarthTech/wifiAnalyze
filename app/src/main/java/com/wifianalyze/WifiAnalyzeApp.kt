package com.wifianalyze

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wifianalyze.data.notification.NotificationHelper
import com.wifianalyze.data.worker.SignalMonitorWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class WifiAnalyzeApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleBackgroundMonitoring()
    }

    private fun scheduleBackgroundMonitoring() {
        val request = PeriodicWorkRequestBuilder<SignalMonitorWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "signal_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
