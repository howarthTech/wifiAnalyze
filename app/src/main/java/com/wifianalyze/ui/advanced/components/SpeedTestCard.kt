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
import com.wifianalyze.data.network.SpeedTestState

@Composable
fun SpeedTestCard(
    speedTestState: SpeedTestState,
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
                Text("Speed Test", style = MaterialTheme.typography.titleMedium)

                when (speedTestState) {
                    is SpeedTestState.Running -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                    else -> {
                        Button(
                            onClick = onRunTest,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (speedTestState is SpeedTestState.Idle) "Run Test" else "Re-test",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            when (speedTestState) {
                is SpeedTestState.Idle -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap Run Test to measure download and upload speed via Cloudflare.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is SpeedTestState.Running -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    val phaseLabel = when (speedTestState.phase) {
                        SpeedTestState.Phase.Download -> "Downloading…"
                        SpeedTestState.Phase.Upload   -> "Uploading…"
                    }
                    Text(
                        text = phaseLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.1f Mbps".format(speedTestState.currentMbps),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is SpeedTestState.Result -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SpeedValue(label = "Download", mbps = speedTestState.downloadMbps)
                        SpeedValue(label = "Upload", mbps = speedTestState.uploadMbps)
                    }
                }

                is SpeedTestState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = speedTestState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedValue(label: String, mbps: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%.1f".format(mbps),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Mbps",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
