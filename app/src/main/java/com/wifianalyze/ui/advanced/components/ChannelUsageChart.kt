package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork

@Composable
fun ChannelUsageChart(
    networks: List<WifiNetwork>,
    connectedChannel: Int,
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

    val channelData = remember(filteredNetworks, selectedBand) {
        buildChannelBuckets(filteredNetworks, selectedBand)
    }

    if (channelData.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Channel Usage", style = MaterialTheme.typography.titleMedium)

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

            val maxCount = channelData.maxOf { it.second }.coerceAtLeast(1)
            val n = channelData.size
            val labelFontSize = if (n <= 13) 9.sp else 8.sp

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val labelAreaHeight = 20.dp.toPx()
                val chartAreaHeight = size.height - labelAreaHeight
                val gap = 3.dp.toPx()
                val barWidth = ((size.width - gap * (n + 1)) / n).coerceAtLeast(4f)

                // Baseline
                drawLine(
                    color = outlineColor.copy(alpha = 0.4f),
                    start = Offset(0f, chartAreaHeight),
                    end = Offset(size.width, chartAreaHeight),
                    strokeWidth = 1.dp.toPx()
                )

                val labelStyle = TextStyle(
                    fontSize = labelFontSize,
                    fontFamily = FontFamily.Monospace,
                    color = onSurface
                )
                val countStyle = TextStyle(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Unspecified // overridden per bar below
                )

                channelData.forEachIndexed { index, (channel, count) ->
                    val x = gap + index * (barWidth + gap)
                    val isConnected = channel == connectedChannel && selectedBand == connectedBand
                    val barColor = if (isConnected) primaryColor else secondaryColor.copy(alpha = 0.7f)

                    if (count > 0) {
                        val barHeight = (count.toFloat() / maxCount) * chartAreaHeight * 0.85f
                        val top = chartAreaHeight - barHeight

                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, top),
                            size = Size(barWidth, barHeight)
                        )

                        // Count label above bar (only when there's room)
                        if (barHeight >= 16.dp.toPx()) {
                            val countText = textMeasurer.measure(
                                count.toString(),
                                countStyle.copy(color = barColor)
                            )
                            drawText(
                                textLayoutResult = countText,
                                topLeft = Offset(
                                    x + barWidth / 2f - countText.size.width / 2f,
                                    top - countText.size.height - 2.dp.toPx()
                                )
                            )
                        }
                    }

                    // Channel label below baseline
                    val chanLabel = textMeasurer.measure(
                        channel.toString(),
                        labelStyle.copy(color = if (isConnected) primaryColor else onSurface)
                    )
                    drawText(
                        textLayoutResult = chanLabel,
                        topLeft = Offset(
                            x + barWidth / 2f - chanLabel.size.width / 2f,
                            chartAreaHeight + 4.dp.toPx()
                        )
                    )
                }
            }

            // Connected channel label + recommended channel
            val recommended = remember(channelData, selectedBand) {
                recommendedChannel(channelData, selectedBand)
            }

            if (connectedBand == selectedBand && connectedChannel > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ch $connectedChannel = your network",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }

            if (recommended != null && recommended != connectedChannel) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Recommended: Ch $recommended (least congested)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else if (recommended != null && recommended == connectedChannel) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "You're already on the best channel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

private fun recommendedChannel(channelData: List<Pair<Int, Int>>, band: WifiBand): Int? {
    if (channelData.isEmpty()) return null
    return when (band) {
        WifiBand.BAND_2_4_GHZ -> {
            // Prefer non-overlapping channels 1, 6, 11
            val preferred = listOf(1, 6, 11)
            val preferredEntries = channelData.filter { it.first in preferred }
            (if (preferredEntries.isNotEmpty()) preferredEntries else channelData)
                .minByOrNull { it.second }?.first
        }
        else -> channelData.minByOrNull { it.second }?.first
    }
}

private fun buildChannelBuckets(
    networks: List<WifiNetwork>,
    band: WifiBand
): List<Pair<Int, Int>> {
    val countByChannel = networks.groupBy { it.channel }.mapValues { it.value.size }
    return when (band) {
        WifiBand.BAND_2_4_GHZ -> (1..13).map { ch -> ch to (countByChannel[ch] ?: 0) }
        else -> countByChannel.entries.sortedBy { it.key }.map { it.key to it.value }
    }
}
