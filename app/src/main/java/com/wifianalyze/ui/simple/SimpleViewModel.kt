package com.wifianalyze.ui.simple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifianalyze.data.local.RoomReadingDao
import com.wifianalyze.data.local.entity.RoomReadingEntity
import com.wifianalyze.data.wifi.ConnectionInfoProvider
import com.wifianalyze.data.wifi.WifiScanner
import com.wifianalyze.domain.CongestionAnalyzer
import com.wifianalyze.domain.IoTReadinessChecker
import com.wifianalyze.domain.SignalMapper
import com.wifianalyze.domain.TipEngine
import com.wifianalyze.domain.model.BandSignalInfo
import com.wifianalyze.domain.model.CongestionLevel
import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.IoTReadiness
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

data class SimpleUiState(
    val isConnected: Boolean = false,
    val ssid: String = "",
    val quality: SignalQuality = SignalQuality.NO_SIGNAL,
    val signalPercent: Float = 0f,
    val signalColor: androidx.compose.ui.graphics.Color = SignalQuality.NO_SIGNAL.color,
    val band: WifiBand = WifiBand.UNKNOWN,
    val bandSignals: List<BandSignalInfo> = emptyList(),
    val iotReadiness: IoTReadiness = IoTReadiness(false, "Checking..."),
    val competingNetworks: Int = 0,
    val congestion: CongestionLevel = CongestionLevel.LOW,
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
    private val tipEngine: TipEngine,
    private val roomReadingDao: RoomReadingDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimpleUiState())
    val uiState: StateFlow<SimpleUiState> = _uiState.asStateFlow()

    val savedReadings: Flow<List<RoomReading>> = roomReadingDao.getAllReadingsRankedBySignal()
        .map { entities -> entities.map { it.toDomain() } }

    init {
        startMonitoring()
        observeData()
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
        val congestion = congestionAnalyzer.analyzeCongestion(networks, connection.ssid)
        val iot = if (connection.isConnected) {
            iotReadinessChecker.check(networks, connection.ssid)
        } else {
            IoTReadiness(false, "Connect to WiFi to check IoT readiness")
        }
        val tips = tipEngine.generateTips(connection, networks)

        // Build per-band signal info for this network from scan results
        val bandSignals = if (connection.isConnected) {
            buildBandSignalInfo(networks, connection)
        } else {
            emptyList()
        }

        _uiState.update {
            SimpleUiState(
                isConnected = connection.isConnected,
                ssid = connection.ssid,
                quality = quality,
                signalPercent = percent,
                signalColor = color,
                band = connection.band,
                bandSignals = bandSignals,
                iotReadiness = iot,
                competingNetworks = competing,
                congestion = congestion,
                tips = tips,
                isScanning = scanning
            )
        }
    }

    private fun buildBandSignalInfo(
        networks: List<WifiNetwork>,
        connection: ConnectionInfo
    ): List<BandSignalInfo> {
        // Find the best signal for each band for the connected network
        val myNetworks = networks.filter { it.ssid == connection.ssid }

        val bands = listOf(WifiBand.BAND_2_4_GHZ, WifiBand.BAND_5_GHZ, WifiBand.BAND_6_GHZ)
        return bands.mapNotNull { band ->
            val best = myNetworks
                .filter { it.band == band }
                .maxByOrNull { it.rssi }
                ?: return@mapNotNull null

            BandSignalInfo(
                band = band,
                rssi = best.rssi,
                quality = signalMapper.mapSignalQuality(best.rssi),
                channel = best.channel,
                isConnectedBand = connection.band == band
            )
        }
    }

    fun saveRoomReading(roomName: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.isConnected) return@launch

            val entity = RoomReadingEntity(
                roomName = roomName,
                rssi = (state.signalPercent * 50 - 90).toInt(),
                ssid = state.ssid,
                frequency = 0,
                channel = 0,
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
        viewModelScope.launch {
            roomReadingDao.deleteReading(reading.toEntity())
        }
    }

    fun clearAllReadings() {
        viewModelScope.launch {
            roomReadingDao.deleteAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wifiScanner.stopScanning()
        connectionInfoProvider.stopMonitoring()
    }

    private fun RoomReadingEntity.toDomain() = RoomReading(
        id = id,
        roomName = roomName,
        rssi = rssi,
        ssid = ssid,
        frequency = frequency,
        channel = channel,
        band = WifiBand.entries.firstOrNull { it.label == band } ?: WifiBand.UNKNOWN,
        quality = SignalQuality.entries.firstOrNull { it.name == quality } ?: SignalQuality.NO_SIGNAL,
        competingNetworks = competingNetworks,
        congestionLevel = CongestionLevel.entries.firstOrNull { it.name == congestionLevel } ?: CongestionLevel.LOW,
        iotReady = iotReady,
        timestamp = timestamp
    )

    private fun RoomReading.toEntity() = RoomReadingEntity(
        id = id,
        roomName = roomName,
        rssi = rssi,
        ssid = ssid,
        frequency = frequency,
        channel = channel,
        band = band.label,
        quality = quality.name,
        competingNetworks = competingNetworks,
        congestionLevel = congestionLevel.name,
        iotReady = iotReady,
        timestamp = timestamp
    )
}
