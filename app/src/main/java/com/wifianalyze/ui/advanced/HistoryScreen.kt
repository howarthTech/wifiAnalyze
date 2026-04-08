package com.wifianalyze.ui.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.data.local.entity.LatencyHistoryEntity
import com.wifianalyze.data.local.entity.SignalHistoryEntity
import com.wifianalyze.data.local.entity.SpeedTestHistoryEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

private const val MIN_DBM = -90f
private const val MAX_DBM = -30f
private const val DBM_RANGE = MAX_DBM - MIN_DBM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsState(initial = emptyList())
    val speedTests by viewModel.speedTestHistory.collectAsState(initial = emptyList())
    val latencyTests by viewModel.latencyHistory.collectAsState(initial = emptyList())
    val range by viewModel.range.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signal History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Range filter chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryRange.entries.forEach { r ->
                        FilterChip(
                            selected = range == r,
                            onClick = { viewModel.setRange(r) },
                            label = { Text(r.label) }
                        )
                    }
                }
            }

            item {
                if (history.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No history yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Signal data is recorded every 5 minutes while the app is open in Advanced Mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    HistoryChart(samples = history, range = range)
                }
            }

            if (history.isNotEmpty()) {
                item {
                    HistoryStatsCard(samples = history)
                }
            }

            if (speedTests.isNotEmpty()) {
                item {
                    SpeedTestHistoryCard(samples = speedTests, range = range)
                }
            }

            if (latencyTests.isNotEmpty()) {
                item {
                    LatencyHistoryCard(samples = latencyTests, range = range)
                }
            }
        }
    }
}

@Composable
private fun HistoryChart(samples: List<SignalHistoryEntity>, range: HistoryRange) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Signal over ${range.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartLeft = 52f
                val chartRight = size.width - 16f
                val chartTop = 12f
                val chartBottom = size.height - 32f
                val chartWidth = chartRight - chartLeft
                val chartHeight = chartBottom - chartTop

                // Quality background bands
                val bands = listOf(
                    Triple(-30f, -50f, Color(0xFF4CAF50)),
                    Triple(-50f, -60f, Color(0xFF8BC34A)),
                    Triple(-60f, -70f, Color(0xFFFFC107)),
                    Triple(-70f, -80f, Color(0xFFFF9800)),
                    Triple(-80f, -90f, Color(0xFFF44336))
                )
                bands.forEach { (maxDbm, minDbm, color) ->
                    val bandTop = chartTop + chartHeight * (1f - (maxDbm - MIN_DBM) / DBM_RANGE)
                    val bandBottom = chartTop + chartHeight * (1f - (minDbm - MIN_DBM) / DBM_RANGE)
                    drawRect(
                        color = color.copy(alpha = 0.06f),
                        topLeft = Offset(chartLeft, bandTop),
                        size = androidx.compose.ui.geometry.Size(chartWidth, bandBottom - bandTop)
                    )
                }

                // Y-axis labels + grid lines
                val labelStyle = TextStyle(fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                listOf(-30, -50, -60, -70, -80, -90).forEach { dbm ->
                    val y = chartTop + chartHeight * (1f - (dbm - MIN_DBM) / DBM_RANGE)
                    if (y in chartTop..chartBottom) {
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(chartLeft, y), Offset(chartRight, y), 1f)
                        val text = textMeasurer.measure("$dbm", labelStyle)
                        drawText(text, topLeft = Offset(0f, y - text.size.height / 2f))
                    }
                }

                if (samples.size >= 2) {
                    val minTime = samples.first().timestamp.toFloat()
                    val maxTime = samples.last().timestamp.toFloat()
                    val timeRange = (maxTime - minTime).coerceAtLeast(1f)

                    val path = Path()
                    samples.forEachIndexed { i, sample ->
                        val x = chartLeft + chartWidth * ((sample.timestamp - minTime) / timeRange)
                        val rssiClamped = sample.rssi.toFloat().coerceIn(MIN_DBM, MAX_DBM)
                        val y = chartTop + chartHeight * (1f - (rssiClamped - MIN_DBM) / DBM_RANGE)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    val lineColor = signalColorFor(samples.last().rssi)
                    drawPath(path, color = Color.Black.copy(alpha = 0.1f),
                        style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(path, color = lineColor,
                        style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    // X-axis time labels
                    val zone = ZoneId.systemDefault()
                    val formatter = if (range == HistoryRange.DAY)
                        DateTimeFormatter.ofPattern("ha").withZone(zone)
                    else
                        DateTimeFormatter.ofPattern("EEE").withZone(zone)

                    val tickCount = if (range == HistoryRange.DAY) 6 else 7
                    repeat(tickCount + 1) { i ->
                        val frac = i.toFloat() / tickCount
                        val ts = (minTime + frac * timeRange).toLong()
                        val x = chartLeft + chartWidth * frac
                        val label = formatter.format(Instant.ofEpochMilli(ts))
                        val text = textMeasurer.measure(label, labelStyle)
                        drawText(text, topLeft = Offset(x - text.size.width / 2f, chartBottom + 4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryStatsCard(samples: List<SignalHistoryEntity>) {
    val avgRssi = samples.map { it.rssi }.average().toInt()
    val minRssi = samples.minOf { it.rssi }
    val maxRssi = samples.maxOf { it.rssi }

    val stabilityLabel: String? = remember(samples) {
        if (samples.size < 5) null
        else {
            val values = samples.map { it.rssi.toFloat() }
            val mean = values.average().toFloat()
            val stddev = sqrt(values.map { (it - mean) * (it - mean) }.average()).toFloat()
            val score = (100f - stddev * 6f).coerceIn(0f, 100f).toInt()
            when {
                score >= 85 -> "Stable ($score/100)"
                score >= 65 -> "Mostly Stable ($score/100)"
                score >= 45 -> "Variable ($score/100)"
                else        -> "Unstable ($score/100)"
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Best", "$maxRssi dBm")
                StatItem("Avg", "$avgRssi dBm")
                StatItem("Worst", "$minRssi dBm")
                StatItem("Samples", "${samples.size}")
            }
            if (stabilityLabel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    StatItem("Stability", stabilityLabel)
                }
            }
        }
    }
}

@Composable
private fun SpeedTestHistoryCard(samples: List<SpeedTestHistoryEntity>, range: HistoryRange) {
    val textMeasurer = rememberTextMeasurer()
    val downloadColor = Color(0xFF4CAF50)
    val uploadColor = Color(0xFF2196F3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Speed Tests over ${range.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(downloadColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(uploadColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (samples.size >= 2) {
                val maxMbps = (samples.maxOf { maxOf(it.downloadMbps, it.uploadMbps) } * 1.1f).coerceAtLeast(10f)
                val labelStyle = TextStyle(fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val chartLeft = 48f
                    val chartRight = size.width - 16f
                    val chartTop = 12f
                    val chartBottom = size.height - 28f
                    val chartWidth = chartRight - chartLeft
                    val chartHeight = chartBottom - chartTop

                    // Y grid lines + labels
                    val ySteps = 4
                    repeat(ySteps + 1) { i ->
                        val frac = i.toFloat() / ySteps
                        val mbps = maxMbps * (1f - frac)
                        val y = chartTop + chartHeight * frac
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(chartLeft, y), Offset(chartRight, y), 1f)
                        val label = textMeasurer.measure("${mbps.toInt()}", labelStyle)
                        drawText(label, topLeft = Offset(0f, y - label.size.height / 2f))
                    }

                    val minTime = samples.first().timestamp.toFloat()
                    val maxTime = samples.last().timestamp.toFloat()
                    val timeRange = (maxTime - minTime).coerceAtLeast(1f)

                    fun xFor(ts: Long) = chartLeft + chartWidth * ((ts - minTime) / timeRange)
                    fun yFor(mbps: Float) = chartTop + chartHeight * (1f - mbps / maxMbps)

                    // Download line
                    val dlPath = Path()
                    samples.forEachIndexed { i, s ->
                        val x = xFor(s.timestamp)
                        val y = yFor(s.downloadMbps)
                        if (i == 0) dlPath.moveTo(x, y) else dlPath.lineTo(x, y)
                    }
                    drawPath(dlPath, downloadColor.copy(alpha = 0.15f),
                        style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(dlPath, downloadColor,
                        style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    // Upload line
                    val ulPath = Path()
                    samples.forEachIndexed { i, s ->
                        val x = xFor(s.timestamp)
                        val y = yFor(s.uploadMbps)
                        if (i == 0) ulPath.moveTo(x, y) else ulPath.lineTo(x, y)
                    }
                    drawPath(ulPath, uploadColor.copy(alpha = 0.15f),
                        style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(ulPath, uploadColor,
                        style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                    // Dots for each data point
                    samples.forEach { s ->
                        drawCircle(downloadColor, 4f, Offset(xFor(s.timestamp), yFor(s.downloadMbps)))
                        drawCircle(uploadColor, 4f, Offset(xFor(s.timestamp), yFor(s.uploadMbps)))
                    }

                    // X-axis time labels
                    val zone = ZoneId.systemDefault()
                    val formatter = if (range == HistoryRange.DAY)
                        DateTimeFormatter.ofPattern("ha").withZone(zone)
                    else
                        DateTimeFormatter.ofPattern("EEE").withZone(zone)

                    samples.forEachIndexed { i, s ->
                        if (i == 0 || i == samples.lastIndex || samples.size <= 5) {
                            val x = xFor(s.timestamp)
                            val label = formatter.format(Instant.ofEpochMilli(s.timestamp))
                            val text = textMeasurer.measure(label, labelStyle)
                            drawText(text, topLeft = Offset(x - text.size.width / 2f, chartBottom + 4f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Recent results list (newest first, max 5)
            Text(
                "Recent Tests",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val zone = ZoneId.systemDefault()
            val timeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(zone)

            samples.takeLast(5).reversed().forEach { sample ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatter.format(Instant.ofEpochMilli(sample.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "↓ ${"%.1f".format(sample.downloadMbps)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = downloadColor
                        )
                        Text(
                            text = "↑ ${"%.1f".format(sample.uploadMbps)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = uploadColor
                        )
                        Text(
                            text = "Mbps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LatencyHistoryCard(samples: List<LatencyHistoryEntity>, range: HistoryRange) {
    val textMeasurer = rememberTextMeasurer()
    val gatewayColor = Color(0xFFFF9800)
    val internetColor = Color(0xFF2196F3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Latency Tests over ${range.label}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LegendDot(gatewayColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gateway", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(internetColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Internet (8.8.8.8)", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            val validSamples = samples.filter { it.gatewayAvgMs != null || it.internetAvgMs != null }

            if (validSamples.size >= 2) {
                val allMs = validSamples.flatMap { s -> listOfNotNull(s.gatewayAvgMs, s.internetAvgMs) }
                val maxMs = ((allMs.maxOrNull() ?: 100f) * 1.15f).coerceAtLeast(10f)
                val labelStyle = TextStyle(fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val chartLeft = 52f
                    val chartRight = size.width - 16f
                    val chartTop = 12f
                    val chartBottom = size.height - 28f
                    val chartWidth = chartRight - chartLeft
                    val chartHeight = chartBottom - chartTop

                    val ySteps = 4
                    repeat(ySteps + 1) { i ->
                        val frac = i.toFloat() / ySteps
                        val ms = maxMs * (1f - frac)
                        val y = chartTop + chartHeight * frac
                        drawLine(Color.Gray.copy(alpha = 0.2f), Offset(chartLeft, y), Offset(chartRight, y), 1f)
                        val label = textMeasurer.measure("${ms.toInt()}", labelStyle)
                        drawText(label, topLeft = Offset(0f, y - label.size.height / 2f))
                    }

                    val minTime = validSamples.first().timestamp.toFloat()
                    val maxTime = validSamples.last().timestamp.toFloat()
                    val timeRange = (maxTime - minTime).coerceAtLeast(1f)

                    fun xFor(ts: Long) = chartLeft + chartWidth * ((ts - minTime) / timeRange)
                    fun yFor(ms: Float) = chartTop + chartHeight * (1f - ms / maxMs)

                    // Gateway line
                    val gwSamples = validSamples.filter { it.gatewayAvgMs != null }
                    if (gwSamples.size >= 2) {
                        val gwPath = Path()
                        gwSamples.forEachIndexed { i, s ->
                            val x = xFor(s.timestamp); val y = yFor(s.gatewayAvgMs!!)
                            if (i == 0) gwPath.moveTo(x, y) else gwPath.lineTo(x, y)
                        }
                        drawPath(gwPath, gatewayColor.copy(alpha = 0.15f),
                            style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(gwPath, gatewayColor,
                            style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        gwSamples.forEach { s ->
                            drawCircle(gatewayColor, 4f, Offset(xFor(s.timestamp), yFor(s.gatewayAvgMs!!)))
                        }
                    }

                    // Internet line
                    val inSamples = validSamples.filter { it.internetAvgMs != null }
                    if (inSamples.size >= 2) {
                        val inPath = Path()
                        inSamples.forEachIndexed { i, s ->
                            val x = xFor(s.timestamp); val y = yFor(s.internetAvgMs!!)
                            if (i == 0) inPath.moveTo(x, y) else inPath.lineTo(x, y)
                        }
                        drawPath(inPath, internetColor.copy(alpha = 0.15f),
                            style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(inPath, internetColor,
                            style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        inSamples.forEach { s ->
                            drawCircle(internetColor, 4f, Offset(xFor(s.timestamp), yFor(s.internetAvgMs!!)))
                        }
                    }

                    // X-axis labels
                    val zone = ZoneId.systemDefault()
                    val formatter = if (range == HistoryRange.DAY)
                        DateTimeFormatter.ofPattern("ha").withZone(zone)
                    else
                        DateTimeFormatter.ofPattern("EEE").withZone(zone)

                    listOf(validSamples.first(), validSamples.last()).distinct().forEach { s ->
                        val x = xFor(s.timestamp)
                        val label = formatter.format(Instant.ofEpochMilli(s.timestamp))
                        val text = textMeasurer.measure(label, labelStyle)
                        drawText(text, topLeft = Offset(x - text.size.width / 2f, chartBottom + 4f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Recent results list
            Text("Recent Tests", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            val zone = ZoneId.systemDefault()
            val timeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(zone)

            validSamples.takeLast(5).reversed().forEach { sample ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatter.format(Instant.ofEpochMilli(sample.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        sample.gatewayAvgMs?.let {
                            Text("GW ${"%.0f".format(it)}ms",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = gatewayColor)
                        }
                        sample.internetAvgMs?.let {
                            Text("IN ${"%.0f".format(it)}ms",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = internetColor)
                        }
                    }
                }
            }
        }
    }
}

private fun signalColorFor(rssi: Int): Color = when {
    rssi >= -50 -> Color(0xFF4CAF50)
    rssi >= -60 -> Color(0xFF8BC34A)
    rssi >= -70 -> Color(0xFFFFC107)
    rssi >= -80 -> Color(0xFFFF9800)
    else        -> Color(0xFFF44336)
}
