package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionDetailsCard(
    ipAddress: String,
    gateway: String,
    dnsServers: List<String>,
    subnetPrefixLength: Int,
    txLinkSpeedMbps: Int,
    rxLinkSpeedMbps: Int,
    linkSpeedMbps: Int,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connection Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        val text = buildString {
                            appendLine("IP Address: ${ipAddress.ifBlank { "—" }}")
                            appendLine("Gateway: ${gateway.ifBlank { "—" }}")
                            appendLine("Subnet: ${if (subnetPrefixLength > 0) "/$subnetPrefixLength" else "—"}")
                            appendLine("DNS: ${dnsServers.ifEmpty { listOf("—") }.joinToString(", ")}")
                            appendLine("Link Speed: $linkSpeedMbps Mbps")
                            if (txLinkSpeedMbps > 0 || rxLinkSpeedMbps > 0) {
                                appendLine("TX Speed: $txLinkSpeedMbps Mbps")
                                appendLine("RX Speed: $rxLinkSpeedMbps Mbps")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(text.trim()))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = "Copy connection details",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("IP Address", ipAddress.ifBlank { "—" })
            DetailRow("Gateway", gateway.ifBlank { "—" })
            DetailRow(
                "Subnet",
                if (subnetPrefixLength > 0) "/$subnetPrefixLength" else "—"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
            )

            DetailRow(
                "DNS",
                dnsServers.ifEmpty { listOf("—") }.joinToString(", ")
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
            )

            DetailRow("Link Speed", "${linkSpeedMbps} Mbps")
            if (txLinkSpeedMbps > 0 || rxLinkSpeedMbps > 0) {
                DetailRow("TX Speed", "${txLinkSpeedMbps} Mbps")
                DetailRow("RX Speed", "${rxLinkSpeedMbps} Mbps")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
