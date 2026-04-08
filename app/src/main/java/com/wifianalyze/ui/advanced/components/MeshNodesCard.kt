package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifianalyze.domain.model.WifiNetwork

@Composable
fun MeshNodesCard(
    networks: List<WifiNetwork>,
    connectedSsid: String,
    connectedBssid: String,
    modifier: Modifier = Modifier
) {
    // All APs broadcasting the connected SSID, grouped by BSSID
    val nodes = remember(networks, connectedSsid) {
        networks
            .filter { it.ssid == connectedSsid }
            .groupBy { it.bssid }
            .map { (_, entries) -> entries.maxBy { it.rssi } }
            .sortedByDescending { it.rssi }
    }

    if (nodes.size < 2) return

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Access Points (${nodes.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Multiple APs detected for \"$connectedSsid\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            nodes.forEachIndexed { index, node ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                NodeRow(node = node, isConnected = node.bssid == connectedBssid)
            }
        }
    }
}

@Composable
private fun NodeRow(node: WifiNetwork, isConnected: Boolean) {
    val signalColor = when {
        node.rssi >= -50 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        node.rssi >= -60 -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
        node.rssi >= -70 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        node.rssi >= -80 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        else             -> androidx.compose.ui.graphics.Color(0xFFF44336)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Router,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isConnected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.bssid,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isConnected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "Connected",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (node.vendorName.isNotBlank()) {
                    Text(
                        text = node.vendorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${node.rssi}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = signalColor
                )
                Text("dBm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = node.band.label,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Text("Ch ${node.channel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
