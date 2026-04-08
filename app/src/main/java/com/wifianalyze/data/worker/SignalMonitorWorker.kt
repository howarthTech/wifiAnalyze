package com.wifianalyze.data.worker

import android.content.Context
import android.net.wifi.WifiManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wifianalyze.data.local.SignalHistoryDao
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import com.wifianalyze.data.notification.NotificationHelper
import com.wifianalyze.data.preferences.AppPreferences
import com.wifianalyze.data.widget.WidgetUpdater
import com.wifianalyze.domain.ChannelHelper
import com.wifianalyze.domain.model.WifiBand
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SignalMonitorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wifiManager: WifiManager,
    private val appPreferences: AppPreferences,
    private val signalHistoryDao: SignalHistoryDao,
    private val notificationHelper: NotificationHelper,
    private val widgetUpdater: WidgetUpdater
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager.connectionInfo ?: return Result.success()
        if (wifiInfo.networkId == -1) return Result.success()

        val rssi = wifiInfo.rssi
        if (rssi == 0 || rssi <= -127) return Result.success()

        @Suppress("DEPRECATION")
        val rawSsid = wifiInfo.ssid ?: return Result.success()
        val ssid = rawSsid.removePrefix("\"").removeSuffix("\"")

        @Suppress("DEPRECATION")
        val freq = wifiInfo.frequency
        val band = ChannelHelper.frequencyToBand(freq)

        // Record to history DB
        signalHistoryDao.insert(
            SignalHistoryEntity(
                timestamp = System.currentTimeMillis(),
                rssi = rssi,
                ssid = ssid,
                band = band.label
            )
        )

        // Prune history older than 7 days
        val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600_000L
        signalHistoryDao.deleteOlderThan(sevenDaysAgo)

        // Check alert threshold
        val alertsEnabled = appPreferences.alertsEnabled.first()
        val threshold = appPreferences.alertThresholdDbm.first()
        if (alertsEnabled && rssi < threshold) {
            notificationHelper.sendSignalAlert(ssid, rssi, threshold)
        }

        // Update home screen widget
        val qualityLabel = when {
            rssi >= -50 -> "Excellent"
            rssi >= -60 -> "Good"
            rssi >= -70 -> "Fair"
            rssi >= -80 -> "Poor"
            else        -> "No Signal"
        }
        widgetUpdater.update(ssid, rssi, qualityLabel)

        // Channel congestion recommendation (once per 24 hours)
        checkChannelCongestion(ssid, freq, band)

        return Result.success()
    }

    @Suppress("DEPRECATION")
    private suspend fun checkChannelCongestion(ssid: String, freq: Int, band: WifiBand) {
        val scanResults = wifiManager.scanResults ?: return
        if (scanResults.isEmpty()) return

        val currentChannel = ChannelHelper.frequencyToChannel(freq)
        if (currentChannel <= 0) return

        val onCurrentChannel = scanResults.count {
            ChannelHelper.frequencyToChannel(it.frequency) == currentChannel
        }

        // Only recommend if 4+ networks on the same channel (current + 3 others)
        if (onCurrentChannel < 4) return

        val suggestedChannel = if (band == WifiBand.BAND_2_4_GHZ) {
            val preferred = listOf(1, 6, 11)
            preferred.minByOrNull { ch ->
                scanResults.count { ChannelHelper.frequencyToChannel(it.frequency) == ch }
            } ?: 1
        } else {
            scanResults
                .groupBy { ChannelHelper.frequencyToChannel(it.frequency) }
                .minByOrNull { it.value.size }
                ?.key ?: currentChannel
        }

        if (suggestedChannel == currentChannel) return

        val lastNotif = appPreferences.lastChannelRecommendationMs.first()
        val now = System.currentTimeMillis()
        if (now - lastNotif < 24 * 3600_000L) return

        appPreferences.setLastChannelRecommendationMs(now)
        notificationHelper.sendChannelRecommendation(
            ssid = ssid,
            currentChannel = currentChannel,
            suggestedChannel = suggestedChannel,
            competingCount = onCurrentChannel - 1
        )
    }
}
