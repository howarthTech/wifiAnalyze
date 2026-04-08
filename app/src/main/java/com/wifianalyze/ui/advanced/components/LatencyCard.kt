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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifianalyze.data.network.LatencyResult
import com.wifianalyze.data.network.PingResult

@Composable
fun LatencyCard(
    latencyResult: LatencyResult,
    onRunTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Network Latency", style = MaterialTheme.typography.titleMedium)

                when (latencyResult) {
                    is LatencyResult.Running -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                    else -> {
                        Button(
                            onClick = onRunTest,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (latencyResult is LatencyResult.Idle) "Run Test" else "Re-test",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            when (latencyResult) {
                is LatencyResult.Idle -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap Run Test to measure ping to your gateway and internet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is LatencyResult.Running -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pinging gateway and 8.8.8.8…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is LatencyResult.Success -> {
                    Spacer(modifier = Modifier.height(12.dp))

                    latencyResult.gateway?.let { result ->
                        PingRow(label = "Gateway", result = result)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    latencyResult.internet?.let { result ->
                        PingRow(label = "Internet", result = result)
                    }

                    if (latencyResult.gateway == null && latencyResult.internet == null) {
                        Text(
                            text = "No response — check network connection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is LatencyResult.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = latencyResult.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PingRow(label: String, result: PingResult) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.host,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RttValue("min", result.minMs)
                Spacer(modifier = Modifier.width(12.dp))
                RttValue("avg", result.avgMs)
                Spacer(modifier = Modifier.width(12.dp))
                RttValue("max", result.maxMs)
            }
        }

        if (result.packetLoss > 0) {
            Text(
                text = "${result.packetLoss}% packet loss",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RttValue(label: String, ms: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%.1f".format(ms),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
