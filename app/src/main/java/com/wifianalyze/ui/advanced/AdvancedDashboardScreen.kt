package com.wifianalyze.ui.advanced

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.ui.advanced.components.ConnectionDetailsCard
import com.wifianalyze.ui.advanced.components.RawSignalCard
import com.wifianalyze.ui.advanced.components.SignalHistoryChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedDashboardScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: AdvancedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                        isConnected = state.isConnected,
                        isInitializing = state.isInitializing
                    )
                }

                if (state.isConnected && !state.isInitializing) {
                    // Signal history chart
                    if (state.signalHistory.size >= 2) {
                        item {
                            SignalHistoryChart(
                                samples = state.signalHistory
                            )
                        }
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

                    // Placeholder for future cards (channel charts, latency, etc.)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
