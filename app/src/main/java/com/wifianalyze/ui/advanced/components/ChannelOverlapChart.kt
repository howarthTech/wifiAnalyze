package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork

private val SPECTRUM_COLORS = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFF4CAF50), // Green
    Color(0xFFFF9800), // Orange
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
    Color(0xFF8BC34A), // Light Green
)

@Composable
fun ChannelOverlapChart(
    networks: List<WifiNetwork>,
    connectedBssid: String,
    connectedBand: WifiBand,
    modifier: Modifier = Modifier
) {
    val availableBands = remember(networks) {
        networks.map { it.band }
            .filter { it != WifiBand.UNKNOWN }
            .distinct()
            .sortedBy { it.ordinal }
    }

    if (availableBands.isEmpty()) return

    val defaultBand = if (connectedBand in availableBands) connectedBand else availableBands.first()
    var selectedBand by remember(availableBands) { mutableStateOf(defaultBand) }

    val filteredNetworks = remember(networks, selectedBand) {
        networks.filter { it.band == selectedBand }
    }

    if (filteredNetworks.isEmpty()) return

    // Stable color assignment per BSSID so colors don't shift on each scan
    val colorByBssid = remember(filteredNetworks) {
        filteredNetworks.mapIndexed { i, n ->
            n.bssid to SPECTRUM_COLORS[i % SPECTRUM_COLORS.size]
        }.toMap()
    }

    // Frequency display range: extend beyond the outermost AP's bandwidth edge
    val (minFreq, maxFreq) = remember(filteredNetworks) {
        val maxWidthMhz = filteredNetworks.maxOf { it.channelWidthMhz }
        val margin = maxWidthMhz / 2 + 20
        filteredNetworks.minOf { it.frequency } - margin to
                filteredNetworks.maxOf { it.frequency } + margin
    }
    val freqSpan = (maxFreq - minFreq).toFloat()

    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Channel Spectrum", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableBands.forEach { band ->
                    FilterChip(
                        selected = selectedBand == band,
                        onClick = { selectedBand = band },
                        label = { Text(band.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val labelStyle = TextStyle(
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = onSurface
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val labelAreaHeight = 20.dp.toPx()
                val chartAreaHeight = size.height - labelAreaHeight
                val chartWidth = size.width

                // Draw networks weakest-first so stronger signals render on top
                filteredNetworks.sortedBy { it.rssi }.forEach { network ->
                    val color = colorByBssid[network.bssid] ?: Color.Gray
                    val isConnected = network.bssid == connectedBssid

                    val centerX = (network.frequency - minFreq) / freqSpan * chartWidth
                    val widthPx = (network.channelWidthMhz / freqSpan) * chartWidth
                    val left = centerX - widthPx / 2f

                    // Height proportional to signal strength: -90 dBm → 0, -30 dBm → full
                    val normalizedHeight = ((network.rssi + 90) / 60f).coerceIn(0.05f, 1f)
                    val barHeight = normalizedHeight * chartAreaHeight * 0.85f
                    val top = chartAreaHeight - barHeight

                    // Semi-transparent fill
                    drawRect(
                        color = color.copy(alpha = if (isConnected) 0.55f else 0.35f),
                        topLeft = Offset(left, top),
                        size = Size(widthPx, barHeight)
                    )

                    // Solid border to highlight the connected AP
                    if (isConnected) {
                        drawRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(widthPx, barHeight),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Baseline
                drawLine(
                    color = outlineColor.copy(alpha = 0.5f),
                    start = Offset(0f, chartAreaHeight),
                    end = Offset(chartWidth, chartAreaHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Channel labels — one per unique channel to avoid overlap
                filteredNetworks.distinctBy { it.channel }.forEach { network ->
                    val centerX = (network.frequency - minFreq) / freqSpan * chartWidth
                    val label = textMeasurer.measure("ch${network.channel}", labelStyle)
                    drawText(
                        textLayoutResult = label,
                        topLeft = Offset(
                            centerX - label.size.width / 2f,
                            chartAreaHeight + 4.dp.toPx()
                        )
                    )
                }
            }

            // Legend: top 6 networks by signal strength
            val legendNetworks = filteredNetworks.sortedByDescending { it.rssi }.take(6)
            if (legendNetworks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    legendNetworks.forEach { network ->
                        val color = colorByBssid[network.bssid] ?: Color.Gray
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Canvas(modifier = Modifier.size(8.dp)) {
                                drawCircle(color = color)
                            }
                            Text(
                                text = buildString {
                                    append(network.ssid.take(16).padEnd(16))
                                    if (network.vendorName.isNotEmpty()) {
                                        append("  ${network.vendorName.take(12).padEnd(12)}")
                                    } else {
                                        append("  ".padEnd(14))
                                    }
                                    append("  ch${network.channel}")
                                    append("  ${network.rssi} dBm")
                                    if (network.bssid == connectedBssid) append("  ★")
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (filteredNetworks.size > 6) {
                        Text(
                            text = "…and ${filteredNetworks.size - 6} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
