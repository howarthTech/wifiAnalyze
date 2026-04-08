package com.wifianalyze.ui.simple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifianalyze.data.local.RoomReadingDao
import com.wifianalyze.data.local.entity.RoomReadingEntity
import com.wifianalyze.data.notification.NotificationHelper
import com.wifianalyze.data.preferences.AppPreferences
import com.wifianalyze.data.widget.WidgetUpdater
import com.wifianalyze.data.wifi.ConnectionInfoProvider
import com.wifianalyze.data.wifi.WifiScanner
import com.wifianalyze.domain.CongestionAnalyzer
import com.wifianalyze.domain.IoTReadinessChecker
import com.wifianalyze.domain.NetworkOptimizer
import com.wifianalyze.domain.SignalMapper
import com.wifianalyze.domain.TipEngine
import com.wifianalyze.domain.model.BandSignalInfo
import com.wifianalyze.domain.model.CongestionLevel
import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.IoTReadiness
import com.wifianalyze.domain.model.Recommendation
import com.wifianalyze.domain.model.RoomReading
import com.wifianalyze.domain.model.SignalQuality
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbyNetworkInfo(
    val ssid: String,
    val quality: SignalQuality,
    val band: WifiBand,
    val channel: Int,
    val rssi: Int,
    val security: String,
    val isYourNetwork: Boolean
)

data class SimpleUiState(
    val isInitializing: Boolean = true,
    val isConnected: Boolean = false,
    val ssid: String = "",
    val rssi: Int = -100,
    val quality: SignalQuality = SignalQuality.NO_SIGNAL,
    val signalPercent: Float = 0f,
    val signalColor: androidx.compose.ui.graphics.Color = SignalQuality.NO_SIGNAL.color,
    val band: WifiBand = WifiBand.UNKNOWN,
    val channel: Int = 0,
    val frequency: Int = 0,
    val bandSignals: List<BandSignalInfo> = emptyList(),
    val iotReadiness: IoTReadiness = IoTReadiness(false, "Checking..."),
    val competingNetworks: Int = 0,
    val topCompetingNames: List<String> = emptyList(),
    val nearbyNetworks: List<NearbyNetworkInfo> = emptyList(),
    val congestion: CongestionLevel = CongestionLevel.LOW,
    val recommendations: List<Recommendation> = emptyList(),
    val tips: List<String> = emptyList(),
    val isScanning: Boolean = false
)

@HiltViewModel
class SimpleViewModel @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val signalMapper: SignalMapper,
    private val congestionAnalyzer: CongestionAnalyzer,
    private val iotReadinessChecker: IoTReadinessChecker,
    private val networkOptimizer: NetworkOptimizer,
    private val tipEngine: TipEngine,
    private val roomReadingDao: RoomReadingDao,
    private val appPreferences: AppPreferences,
    private val notificationHelper: NotificationHelper,
    private val widgetUpdater: WidgetUpdater,
    private val wearDataSender: com.wifianalyze.data.wear.WearDataSender
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimpleUiState())
    val uiState: StateFlow<SimpleUiState> = _uiState.asStateFlow()

    val savedReadings: Flow<List<RoomReading>> = roomReadingDao.getAllReadingsRankedBySignal()
        .map { entities -> entities.map { it.toDomain() } }

    // Alert settings (observed locally for use in updateState)
    private var alertsEnabled = false
    private var alertThresholdDbm = -75
    private var lastAlertTime = 0L
    private val alertCooldownMs = 60_000L
    private var wasSignalBelowThreshold = false

    init {
        startMonitoring()
        observeData()
        observeAlertPrefs()

        viewModelScope.launch {
            delay(3_000)
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    private fun observeAlertPrefs() {
        viewModelScope.launch { appPreferences.alertsEnabled.collect { alertsEnabled = it } }
        viewModelScope.launch { appPreferences.alertThresholdDbm.collect { alertThresholdDbm = it } }
    }

    private fun startMonitoring() {
        connectionInfoProvider.startMonitoring()
        wifiScanner.startScan()

        viewModelScope.launch {
            while (true) {
                delay(10_000)
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
        val topNames = networks
            .filter { it.ssid != connection.ssid && it.ssid.isNotBlank() }
            .distinctBy { it.ssid }
            .sortedByDescending { it.rssi }
            .take(5)
            .map { it.ssid }
        val congestion = congestionAnalyzer.analyzeCongestion(networks, connection.ssid)
        val iot = if (connection.isConnected) {
            iotReadinessChecker.check(networks, connection.ssid)
        } else {
            IoTReadiness(false, "Connect to WiFi to check IoT readiness")
        }
        val recs = networkOptimizer.analyze(connection, networks)
        val tips = tipEngine.generateTips(connection, networks)
        val bandSignals = if (connection.isConnected) buildBandSignalInfo(networks, connection) else emptyList()
        val nearby = networks
            .filter { it.ssid.isNotBlank() }
            .groupBy { it.ssid }
            .map { (ssid, entries) ->
                val best = entries.maxBy { it.rssi }
                NearbyNetworkInfo(
                    ssid = ssid,
                    quality = signalMapper.mapSignalQuality(best.rssi),
                    band = best.band,
                    channel = best.channel,
                    rssi = best.rssi,
                    security = parseSecurityType(best.capabilities),
                    isYourNetwork = ssid == connection.ssid
                )
            }
            .sortedByDescending { it.rssi }

        val hasScanData = networks.isNotEmpty()
        val stillInitializing = _uiState.value.isInitializing && !(connection.isConnected && hasScanData)

        _uiState.update {
            SimpleUiState(
                isInitializing = stillInitializing,
                isConnected = connection.isConnected,
                ssid = connection.ssid,
                rssi = connection.rssi,
                quality = quality,
                signalPercent = percent,
                signalColor = color,
                band = connection.band,
                channel = connection.channel,
                frequency = connection.frequency,
                bandSignals = bandSignals,
                iotReadiness = iot,
                competingNetworks = competing,
                topCompetingNames = topNames,
                nearbyNetworks = nearby,
                congestion = congestion,
                recommendations = recs,
                tips = tips,
                isScanning = scanning
            )
        }

        // Widget update
        if (connection.isConnected) {
            widgetUpdater.update(connection.ssid, connection.rssi, quality.label)
        }

        // Wear OS update
        viewModelScope.launch {
            if (connection.isConnected) {
                wearDataSender.send(
                    ssid = connection.ssid,
                    rssi = connection.rssi,
                    quality = quality.label,
                    band = connection.band?.toString() ?: ""
                )
            } else {
                wearDataSender.sendDisconnected()
            }
        }

        // Notification alert check with all-clear
        if (alertsEnabled) {
            if (connection.isConnected) {
                val isBelowThreshold = connection.rssi < alertThresholdDbm
                if (isBelowThreshold && !wasSignalBelowThreshold) {
                    wasSignalBelowThreshold = true
                    val now = System.currentTimeMillis()
                    if (now - lastAlertTime > alertCooldownMs) {
                        lastAlertTime = now
                        notificationHelper.sendSignalAlert(connection.ssid, connection.rssi, alertThresholdDbm)
                    }
                } else if (!isBelowThreshold && wasSignalBelowThreshold) {
                    wasSignalBelowThreshold = false
                    notificationHelper.sendSignalClearAlert(connection.ssid, connection.rssi, alertThresholdDbm)
                }
            } else {
                wasSignalBelowThreshold = false
            }
        }
    }

    private fun buildBandSignalInfo(
        networks: List<WifiNetwork>,
        connection: ConnectionInfo
    ): List<BandSignalInfo> {
        val myNetworks = networks.filter { it.ssid == connection.ssid }
        val bands = listOf(WifiBand.BAND_2_4_GHZ, WifiBand.BAND_5_GHZ, WifiBand.BAND_6_GHZ)
        return bands.mapNotNull { band ->
            val best = myNetworks.filter { it.band == band }.maxByOrNull { it.rssi } ?: return@mapNotNull null
            BandSignalInfo(
                band = band,
                rssi = best.rssi,
                quality = signalMapper.mapSignalQuality(best.rssi),
                channel = best.channel,
                isConnectedBand = connection.band == band
            )
        }
    }

    fun refresh() {
        wifiScanner.startScan()
        connectionInfoProvider.refreshConnectionInfo()
    }

    fun saveRoomReading(roomName: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isConnected) return@launch
            val entity = RoomReadingEntity(
                roomName = roomName,
                rssi = state.rssi,
                ssid = state.ssid,
                frequency = state.frequency,
                channel = state.channel,
                band = state.band.label,
                quality = state.quality.name,
                competingNetworks = state.competingNetworks,
                congestionLevel = state.congestion.name,
                iotReady = state.iotReadiness.ready,
                timestamp = System.currentTimeMillis()
            )
            roomReadingDao.insertReading(entity)
        }
    }

    fun deleteReading(reading: RoomReading) {
        viewModelScope.launch { roomReadingDao.deleteReading(reading.toEntity()) }
    }

    fun clearAllReadings() {
        viewModelScope.launch { roomReadingDao.deleteAll() }
    }

    override fun onCleared() {
        super.onCleared()
        wifiScanner.stopScanning()
        connectionInfoProvider.stopMonitoring()
    }

    private fun RoomReadingEntity.toDomain() = RoomReading(
        id = id, roomName = roomName, rssi = rssi, ssid = ssid,
        frequency = frequency, channel = channel,
        band = WifiBand.entries.firstOrNull { it.label == band } ?: WifiBand.UNKNOWN,
        quality = SignalQuality.entries.firstOrNull { it.name == quality } ?: SignalQuality.NO_SIGNAL,
        competingNetworks = competingNetworks,
        congestionLevel = CongestionLevel.entries.firstOrNull { it.name == congestionLevel } ?: CongestionLevel.LOW,
        iotReady = iotReady, timestamp = timestamp
    )

    private fun RoomReading.toEntity() = RoomReadingEntity(
        id = id, roomName = roomName, rssi = rssi, ssid = ssid,
        frequency = frequency, channel = channel, band = band.label,
        quality = quality.name, competingNetworks = competingNetworks,
        congestionLevel = congestionLevel.name, iotReady = iotReady, timestamp = timestamp
    )

    private fun parseSecurityType(capabilities: String): String {
        return when {
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") && capabilities.contains("WPA3") -> "WPA2/WPA3"
            capabilities.contains("WPA2") -> "WPA2"
            capabilities.contains("WPA")  -> "WPA"
            capabilities.contains("WEP")  -> "WEP"
            capabilities.contains("ESS") && !capabilities.contains("WPA") && !capabilities.contains("WEP") -> "Open"
            else -> "Unknown"
        }
    }
}
