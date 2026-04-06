package com.wifianalyze.data.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.wifianalyze.domain.ChannelHelper
import com.wifianalyze.domain.model.WifiNetwork
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WifiAnalyze"

@Singleton
class WifiScannerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager
) : WifiScanner {

    private val _scanResults = MutableStateFlow<List<WifiNetwork>>(emptyList())
    override val scanResults: StateFlow<List<WifiNetwork>> = _scanResults.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    override fun startScan() {
        if (receiver == null) {
            registerReceiver()
        }

        _isScanning.value = true

        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()
        if (!success) {
            processScanResults()
        }
    }

    override fun stopScanning() {
        _isScanning.value = false
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: IllegalArgumentException) {
                // Already unregistered
            }
            receiver = null
        }
    }

    private fun registerReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    Log.d(TAG, "Scan broadcast received, success=$success")
                    processScanResults()
                }
            }
        }

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        // Must use RECEIVER_EXPORTED for system broadcasts on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    private fun processScanResults() {
        val results = try {
            wifiManager.scanResults
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException getting scanResults", e)
            emptyList()
        }

        Log.d(TAG, "Raw scan results count: ${results.size}")

        val networks = results.mapNotNull { scanResult ->
            val ssid = scanResult.wifiSsid?.toString()?.removeSurrounding("\"") ?: return@mapNotNull null
            if (ssid.isBlank()) return@mapNotNull null

            val freq = scanResult.frequency
            WifiNetwork(
                ssid = ssid,
                bssid = scanResult.BSSID ?: "",
                rssi = scanResult.level,
                frequency = freq,
                channel = ChannelHelper.frequencyToChannel(freq),
                band = ChannelHelper.frequencyToBand(freq),
                capabilities = scanResult.capabilities ?: ""
            )
        }

        Log.d(TAG, "Filtered networks: ${networks.size} - ${networks.map { "${it.ssid}(${it.band})" }}")
        _scanResults.value = networks
        _isScanning.value = false
    }
}
