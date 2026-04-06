package com.wifianalyze.ui.advanced

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifianalyze.data.wifi.ConnectionInfoProvider
import com.wifianalyze.data.wifi.WifiScanner
import com.wifianalyze.domain.CongestionAnalyzer
import com.wifianalyze.domain.SignalMapper
import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.SignalQuality
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignalSample(
    val timestamp: Long,
    val rssi: Int
)

data class AdvancedUiState(
    val isInitializing: Boolean = true,
    val isConnected: Boolean = false,
    val ssid: String = "",
    val bssid: String = "",
    val rssi: Int = -100,
    val quality: SignalQuality = SignalQuality.NO_SIGNAL,
    val signalPercent: Float = 0f,
    val signalColor: Color = SignalQuality.NO_SIGNAL.color,
    val band: WifiBand = WifiBand.UNKNOWN,
    val channel: Int = 0,
    val frequency: Int = 0,
    val linkSpeedMbps: Int = 0,
    val ipAddress: String = "",
    val gateway: String = "",
    val dnsServers: List<String> = emptyList(),
    val subnetPrefixLength: Int = 0,
    val txLinkSpeedMbps: Int = 0,
    val rxLinkSpeedMbps: Int = 0,
    val signalHistory: List<SignalSample> = emptyList(),
    val networks: List<WifiNetwork> = emptyList(),
    val competingCount: Int = 0,
    val isScanning: Boolean = false
)

@HiltViewModel
class AdvancedViewModel @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val signalMapper: SignalMapper,
    private val congestionAnalyzer: CongestionAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedUiState())
    val uiState: StateFlow<AdvancedUiState> = _uiState.asStateFlow()

    private val signalSamples = mutableListOf<SignalSample>()
    private val maxSamples = 120 // 10 minutes at 5-sec intervals

    init {
        startMonitoring()
        observeData()

        viewModelScope.launch {
            delay(3_000)
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    private fun startMonitoring() {
        connectionInfoProvider.startMonitoring()
        wifiScanner.startScan()

        // Faster scan interval for Advanced Mode
        viewModelScope.launch {
            while (true) {
                delay(5_000)
                wifiScanner.startScan()
                connectionInfoProvider.refreshConnectionInfo()
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                connectionInfoProvider.connectionInfo,
                wifiScanner.scanResults,
                wifiScanner.isScanning
            ) { connection, networks, scanning ->
                Triple(connection, networks, scanning)
            }.collect { (connection, networks, scanning) ->
                updateState(connection, networks, scanning)
            }
        }
    }

    private fun updateState(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>,
        scanning: Boolean
    ) {
        val quality = signalMapper.mapSignalQuality(connection.rssi)
        val color = signalMapper.signalColor(connection.rssi)
        val percent = signalMapper.signalPercentage(connection.rssi)
        val competing = congestionAnalyzer.countCompetingNetworks(networks, connection.ssid)

        // Record signal sample
        if (connection.isConnected) {
            signalSamples.add(SignalSample(System.currentTimeMillis(), connection.rssi))
            if (signalSamples.size > maxSamples) {
                signalSamples.removeAt(0)
            }
        }

        val hasScanData = networks.isNotEmpty()
        val stillInitializing = _uiState.value.isInitializing && !(connection.isConnected && hasScanData)

        _uiState.update {
            AdvancedUiState(
                isInitializing = stillInitializing,
                isConnected = connection.isConnected,
                ssid = connection.ssid,
                bssid = connection.bssid,
                rssi = connection.rssi,
                quality = quality,
                signalPercent = percent,
                signalColor = color,
                band = connection.band,
                channel = connection.channel,
                frequency = connection.frequency,
                linkSpeedMbps = connection.linkSpeedMbps,
                ipAddress = connection.ipAddress,
                gateway = connection.gateway,
                dnsServers = connection.dnsServers,
                subnetPrefixLength = connection.subnetPrefixLength,
                txLinkSpeedMbps = connection.txLinkSpeedMbps,
                rxLinkSpeedMbps = connection.rxLinkSpeedMbps,
                signalHistory = signalSamples.toList(),
                networks = networks,
                competingCount = competing,
                isScanning = scanning
            )
        }
    }

    fun refresh() {
        wifiScanner.startScan()
        connectionInfoProvider.refreshConnectionInfo()
    }

    override fun onCleared() {
        super.onCleared()
        wifiScanner.stopScanning()
        connectionInfoProvider.stopMonitoring()
    }
}
