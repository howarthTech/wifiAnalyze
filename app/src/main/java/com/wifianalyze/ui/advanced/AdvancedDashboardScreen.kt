package com.wifianalyze.ui.advanced

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.ui.advanced.components.ChannelOverlapChart
import com.wifianalyze.ui.advanced.components.ChannelUsageChart
import com.wifianalyze.ui.advanced.components.ConnectionDetailsCard
import com.wifianalyze.ui.advanced.components.LatencyCard
import com.wifianalyze.ui.advanced.components.MeshNodesCard
import com.wifianalyze.ui.advanced.components.NetworkScoreCard
import com.wifianalyze.ui.advanced.components.RawSignalCard
import com.wifianalyze.ui.advanced.components.SpeedTestCard
import com.wifianalyze.ui.advanced.components.SignalHistoryChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: AdvancedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WiFi Analyze")
                        if (state.isConnected) {
                            Text(
                                text = "${state.ssid} • Advanced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Advanced Mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Rounded.History, contentDescription = "Signal History")
                    }
                    if (state.isConnected && !state.isInitializing) {
                        IconButton(onClick = {
                            val json = viewModel.buildExportJson()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, json)
                                putExtra(Intent.EXTRA_SUBJECT, "WiFi Scan — ${state.ssid}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share WiFi Scan"))
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Export / Share")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
                scope.launch {
                    delay(1500)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Raw signal dBm card
                item {
                    RawSignalCard(
                        rssi = state.rssi,
                        quality = state.quality,
                        signalColor = state.signalColor,
                        frequency = state.frequency,
                        channel = state.channel,
                        band = state.band,
                        linkSpeedMbps = state.linkSpeedMbps,
                        bssid = state.bssid,
                        vendorName = state.networks
                            .firstOrNull { it.bssid == state.bssid }?.vendorName ?: "",
                        isConnected = state.isConnected,
                        isInitializing = state.isInitializing,
                        stabilityScore = state.stabilityScore,
                        stabilityLabel = state.stabilityLabel
                    )
                }

                // Network score card (shows once connected and stability is available)
                state.networkScore?.let { score ->
                    item {
                        NetworkScoreCard(score = score)
                    }
                }

                if (state.isConnected && !state.isInitializing) {
                    // Signal history chart
                    if (state.signalHistory.size >= 2) {
                        item {
                            SignalHistoryChart(samples = state.signalHistory)
                        }
                    }

                    // Channel usage bar chart
                    if (state.networks.isNotEmpty()) {
                        item {
                            ChannelUsageChart(
                                networks = state.networks,
                                connectedChannel = state.channel,
                                connectedBand = state.band
                            )
                        }
                    }

                    // Channel spectrum / overlap view
                    if (state.networks.isNotEmpty()) {
                        item {
                            ChannelOverlapChart(
                                networks = state.networks,
                                connectedBssid = state.bssid,
                                connectedBand = state.band
                            )
                        }
                    }

                    // Mesh / multi-AP nodes
                    item {
                        MeshNodesCard(
                            networks = state.networks,
                            connectedSsid = state.ssid,
                            connectedBssid = state.bssid
                        )
                    }

                    // Network latency test
                    item {
                        LatencyCard(
                            latencyResult = state.latencyResult,
                            onRunTest = { viewModel.runLatencyTest() }
                        )
                    }

                    // Speed test
                    item {
                        SpeedTestCard(
                            speedTestState = state.speedTestState,
                            onRunTest = { viewModel.runSpeedTest() }
                        )
                    }

                    // Connection details
                    item {
                        ConnectionDetailsCard(
                            ipAddress = state.ipAddress,
                            gateway = state.gateway,
                            dnsServers = state.dnsServers,
                            subnetPrefixLength = state.subnetPrefixLength,
                            txLinkSpeedMbps = state.txLinkSpeedMbps,
                            rxLinkSpeedMbps = state.rxLinkSpeedMbps,
                            linkSpeedMbps = state.linkSpeedMbps
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
