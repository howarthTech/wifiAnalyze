package com.wifianalyze.ui.advanced

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifianalyze.data.local.LatencyHistoryDao
import com.wifianalyze.data.local.SignalHistoryDao
import com.wifianalyze.data.local.SpeedTestHistoryDao
import com.wifianalyze.data.local.entity.LatencyHistoryEntity
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import com.wifianalyze.data.local.entity.SpeedTestHistoryEntity
import com.wifianalyze.data.network.LatencyResult
import com.wifianalyze.data.network.LatencyTestRunner
import com.wifianalyze.data.network.PingResult
import com.wifianalyze.data.network.SpeedTestRunner
import com.wifianalyze.data.network.SpeedTestState
import com.wifianalyze.data.widget.WidgetUpdater
import com.wifianalyze.data.wifi.ConnectionInfoProvider
import com.wifianalyze.data.wifi.WifiScanner
import com.wifianalyze.domain.CongestionAnalyzer
import com.wifianalyze.domain.SignalMapper
import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.SignalQuality
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class SignalSample(
    val timestamp: Long,
    val rssi: Int
)

data class NetworkScore(
    val score: Int,
    val maxScore: Int,
    val grade: String,
    val gradeColor: Color,
    val signalPoints: Int,
    val stabilityPoints: Int?,
    val speedPoints: Int?,
    val latencyPoints: Int?
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
    val isScanning: Boolean = false,
    val latencyResult: LatencyResult = LatencyResult.Idle,
    val speedTestState: SpeedTestState = SpeedTestState.Idle,
    val stabilityScore: Int? = null,
    val stabilityLabel: String = "",
    val networkScore: NetworkScore? = null
)

@HiltViewModel
class AdvancedViewModel @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val signalMapper: SignalMapper,
    private val congestionAnalyzer: CongestionAnalyzer,
    private val latencyTestRunner: LatencyTestRunner,
    private val speedTestRunner: SpeedTestRunner,
    private val signalHistoryDao: SignalHistoryDao,
    private val speedTestHistoryDao: SpeedTestHistoryDao,
    private val latencyHistoryDao: LatencyHistoryDao,
    private val widgetUpdater: WidgetUpdater
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedUiState())
    val uiState: StateFlow<AdvancedUiState> = _uiState.asStateFlow()

    private val signalSamples = mutableListOf<SignalSample>()
    private val maxSamples = 120 // 10 minutes at 5-sec intervals

    // History DB writes every 5 minutes
    private var lastHistoryWrite = 0L
    private val historyWriteIntervalMs = 5 * 60_000L

    init {
        startMonitoring()
        observeData()
        pruneOldHistory()

        viewModelScope.launch {
            delay(3_000)
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    private fun pruneOldHistory() {
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 3600_000L
            signalHistoryDao.deleteOlderThan(sevenDaysAgo)
            speedTestHistoryDao.deleteOlderThan(sevenDaysAgo)
            latencyHistoryDao.deleteOlderThan(sevenDaysAgo)
        }
    }

    private fun startMonitoring() {
        connectionInfoProvider.startMonitoring()
        wifiScanner.startScan()

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

        if (connection.isConnected) {
            signalSamples.add(SignalSample(System.currentTimeMillis(), connection.rssi))
            if (signalSamples.size > maxSamples) signalSamples.removeAt(0)

            // Write to history DB every 5 minutes
            val now = System.currentTimeMillis()
            if (now - lastHistoryWrite >= historyWriteIntervalMs) {
                lastHistoryWrite = now
                viewModelScope.launch {
                    signalHistoryDao.insert(
                        SignalHistoryEntity(
                            timestamp = now,
                            rssi = connection.rssi,
                            ssid = connection.ssid,
                            band = connection.band.label
                        )
                    )
                }
            }

            widgetUpdater.update(connection.ssid, connection.rssi, quality.label)
        }

        val stability = computeStability(signalSamples)
        val hasScanData = networks.isNotEmpty()
        val stillInitializing = _uiState.value.isInitializing && !(connection.isConnected && hasScanData)

        _uiState.update { current ->
            val updated = current.copy(
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
                isScanning = scanning,
                stabilityScore = stability?.first,
                stabilityLabel = stability?.second ?: ""
            )
            updated.copy(networkScore = computeNetworkScore(updated))
        }
    }

    private fun computeStability(samples: List<SignalSample>): Pair<Int, String>? {
        if (samples.size < 5) return null
        val values = samples.map { it.rssi.toFloat() }
        val mean = values.average().toFloat()
        val stddev = sqrt(values.map { (it - mean) * (it - mean) }.average()).toFloat()
        val score = (100f - stddev * 6f).coerceIn(0f, 100f).toInt()
        val label = when {
            score >= 85 -> "Stable"
            score >= 65 -> "Mostly Stable"
            score >= 45 -> "Variable"
            else        -> "Unstable"
        }
        return score to label
    }

    private fun computeNetworkScore(state: AdvancedUiState): NetworkScore? {
        if (!state.isConnected || state.isInitializing) return null

        val signalPts = (state.signalPercent * 40f).toInt().coerceIn(0, 40)

        val stabilityPts = state.stabilityScore?.let { s ->
            (s / 100f * 20f).toInt().coerceIn(0, 20)
        }

        val speedPts = (state.speedTestState as? SpeedTestState.Result)?.let { r ->
            when {
                r.downloadMbps >= 200 -> 20
                r.downloadMbps >= 100 -> 17
                r.downloadMbps >= 50  -> 14
                r.downloadMbps >= 25  -> 10
                r.downloadMbps >= 10  -> 6
                else                  -> 2
            }
        }

        val latencyPts = (state.latencyResult as? LatencyResult.Success)?.internet?.let { ping ->
            when {
                ping.avgMs <= 20f  -> 20
                ping.avgMs <= 50f  -> 15
                ping.avgMs <= 100f -> 10
                ping.avgMs <= 200f -> 5
                else               -> 1
            }
        }

        val score = signalPts +
            (stabilityPts ?: 0) +
            (speedPts ?: 0) +
            (latencyPts ?: 0)

        val maxScore = 40 +
            (if (stabilityPts != null) 20 else 0) +
            (if (speedPts != null) 20 else 0) +
            (if (latencyPts != null) 20 else 0)

        val pct = if (maxScore > 0) score * 100 / maxScore else 0
        val (grade, gradeColor) = when {
            pct >= 90 -> "A" to Color(0xFF4CAF50)
            pct >= 75 -> "B" to Color(0xFF8BC34A)
            pct >= 60 -> "C" to Color(0xFFFFC107)
            pct >= 45 -> "D" to Color(0xFFFF9800)
            else      -> "F" to Color(0xFFF44336)
        }

        return NetworkScore(
            score = score,
            maxScore = maxScore,
            grade = grade,
            gradeColor = gradeColor,
            signalPoints = signalPts,
            stabilityPoints = stabilityPts,
            speedPoints = speedPts,
            latencyPoints = latencyPts
        )
    }

    fun refresh() {
        wifiScanner.startScan()
        connectionInfoProvider.refreshConnectionInfo()
    }

    fun runLatencyTest() {
        val gateway = _uiState.value.gateway
        if (gateway.isBlank()) return

        _uiState.update { it.copy(latencyResult = LatencyResult.Running) }

        viewModelScope.launch {
            val gatewayResult = latencyTestRunner.ping(gateway)
            val internetResult = latencyTestRunner.ping("8.8.8.8")
            val success = LatencyResult.Success(gateway = gatewayResult, internet = internetResult)
            _uiState.update { current ->
                val updated = current.copy(latencyResult = success)
                updated.copy(networkScore = computeNetworkScore(updated))
            }

            latencyHistoryDao.insert(
                LatencyHistoryEntity(
                    timestamp = System.currentTimeMillis(),
                    gatewayAvgMs = gatewayResult?.avgMs,
                    internetAvgMs = internetResult?.avgMs,
                    ssid = _uiState.value.ssid
                )
            )
        }
    }

    fun runSpeedTest() {
        if (_uiState.value.speedTestState is SpeedTestState.Running) return

        viewModelScope.launch {
            val result = speedTestRunner.runTest { progress ->
                _uiState.update { it.copy(speedTestState = progress) }
            }
            _uiState.update { current ->
                val updated = current.copy(speedTestState = result)
                updated.copy(networkScore = computeNetworkScore(updated))
            }

            if (result is SpeedTestState.Result) {
                speedTestHistoryDao.insert(
                    SpeedTestHistoryEntity(
                        timestamp = System.currentTimeMillis(),
                        downloadMbps = result.downloadMbps,
                        uploadMbps = result.uploadMbps,
                        ssid = _uiState.value.ssid
                    )
                )
            }
        }
    }

    /** Builds a JSON string of the current scan snapshot for sharing. */
    fun buildExportJson(): String {
        val state = _uiState.value
        val ts = Instant.now().toString()

        val networksJson = state.networks.joinToString(",\n    ") { n ->
            """{"ssid":"${n.ssid.jsonEscape()}","bssid":"${n.bssid}","vendor":"${n.vendorName.jsonEscape()}","rssi":${n.rssi},"band":"${n.band.label}","channel":${n.channel},"channelWidthMhz":${n.channelWidthMhz}}"""
        }

        val latencyJson = when (val lr = state.latencyResult) {
            is LatencyResult.Success -> buildString {
                append("{")
                val parts = mutableListOf<String>()
                lr.gateway?.let { parts.add(it.toJson("gateway")) }
                lr.internet?.let { parts.add(it.toJson("internet")) }
                append(parts.joinToString(","))
                append("}")
            }
            else -> "null"
        }

        val speedJson = when (val st = state.speedTestState) {
            is SpeedTestState.Result -> """{"downloadMbps":${"%.2f".format(st.downloadMbps)},"uploadMbps":${"%.2f".format(st.uploadMbps)}}"""
            else -> "null"
        }

        val dnsJson = state.dnsServers.joinToString(",") { "\"$it\"" }

        return """
{
  "timestamp": "$ts",
  "connectedNetwork": {
    "ssid": "${state.ssid.jsonEscape()}",
    "bssid": "${state.bssid}",
    "rssi": ${state.rssi},
    "quality": "${state.quality.label}",
    "band": "${state.band.label}",
    "channel": ${state.channel},
    "frequency": ${state.frequency},
    "ipAddress": "${state.ipAddress}",
    "gateway": "${state.gateway}",
    "dns": [$dnsJson],
    "linkSpeedMbps": ${state.linkSpeedMbps}
  },
  "nearbyNetworks": [
    $networksJson
  ],
  "latency": $latencyJson,
  "speedTest": $speedJson
}""".trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        wifiScanner.stopScanning()
        connectionInfoProvider.stopMonitoring()
    }
}

private fun String.jsonEscape() = replace("\\", "\\\\").replace("\"", "\\\"")

private fun PingResult.toJson(key: String) =
    """"$key":{"host":"$host","minMs":$minMs,"avgMs":$avgMs,"maxMs":$maxMs,"packetLoss":$packetLoss}"""
