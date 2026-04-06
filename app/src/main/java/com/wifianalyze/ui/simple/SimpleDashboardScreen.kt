package com.wifianalyze.ui.simple

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLocation
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.ui.simple.components.CompetingNetworksCard
import com.wifianalyze.ui.simple.components.IoTReadinessCard
import com.wifianalyze.ui.simple.components.NearbyNetworksCard
import com.wifianalyze.ui.simple.components.RecommendationsCard
import com.wifianalyze.ui.simple.components.SignalQualityCard
import com.wifianalyze.ui.simple.components.TipBanner
import com.wifianalyze.ui.simple.components.YourNetworkBandsCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDashboardScreen(
    onNavigateToRoomTest: () -> Unit,
    onNavigateToRoomList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: SimpleViewModel = hiltViewModel()
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
                                text = state.ssid,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.refresh()
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToRoomList) {
                        Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "Saved Rooms")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isConnected) {
                FloatingActionButton(onClick = onNavigateToRoomTest) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.AddLocation, contentDescription = null)
                        Text("Test Room", fontWeight = FontWeight.Medium)
                    }
                }
            }
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
                item {
                    SignalQualityCard(
                        quality = state.quality,
                        signalPercent = state.signalPercent,
                        signalColor = state.signalColor,
                        isConnected = state.isConnected,
                        isScanning = state.isScanning
                    )
                }

                if (state.isConnected) {
                    item {
                        YourNetworkBandsCard(
                            bandSignals = state.bandSignals,
                            connectedBand = state.band
                        )
                    }

                    item {
                        IoTReadinessCard(readiness = state.iotReadiness)
                    }

                    item {
                        CompetingNetworksCard(
                            count = state.competingNetworks,
                            congestion = state.congestion,
                            topNetworkNames = state.topCompetingNames
                        )
                    }

                    item {
                        NearbyNetworksCard(
                            networks = state.nearbyNetworks
                        )
                    }

                    if (state.recommendations.isNotEmpty()) {
                        item {
                            RecommendationsCard(
                                recommendations = state.recommendations
                            )
                        }
                    }

                    items(state.tips) { tip ->
                        TipBanner(tip = tip)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}
