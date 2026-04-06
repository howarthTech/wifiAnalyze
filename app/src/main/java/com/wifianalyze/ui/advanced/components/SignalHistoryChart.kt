package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifianalyze.ui.advanced.SignalSample

// dBm range for the chart
private const val MIN_DBM = -90f
private const val MAX_DBM = -30f
private const val DBM_RANGE = MAX_DBM - MIN_DBM

// Quality threshold colors
private val ExcellentColor = Color(0xFF4CAF50)
private val GoodColor = Color(0xFF8BC34A)
private val FairColor = Color(0xFFFFC107)
private val PoorColor = Color(0xFFFF9800)
private val NoSignalColor = Color(0xFFF44336)

@Composable
fun SignalHistoryChart(
    samples: List<SignalSample>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Signal Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val latestRssi = samples.lastOrNull()?.rssi ?: 0
            Text(
                text = "Current: ${latestRssi} dBm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val chartLeft = 48f
                val chartRight = size.width - 16f
                val chartTop = 12f
                val chartBottom = size.height - 28f
                val chartWidth = chartRight - chartLeft
                val chartHeight = chartBottom - chartTop

                // Draw quality background bands
                drawQualityBands(chartLeft, chartTop, chartWidth, chartHeight)

                // Draw Y-axis labels
                val yLabels = listOf(-30, -50, -60, -70, -80, -90)
                val labelStyle = TextStyle(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
                yLabels.forEach { dbm ->
                    val y = chartTop + chartHeight * (1f - (dbm - MIN_DBM) / DBM_RANGE)
                    if (y in chartTop..chartBottom) {
                        // Grid line
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(chartLeft, y),
                            end = Offset(chartRight, y),
                            strokeWidth = 1f
                        )
                        // Label
                        val text = textMeasurer.measure("$dbm", labelStyle)
                        drawText(
                            textLayoutResult = text,
                            topLeft = Offset(0f, y - text.size.height / 2f)
                        )
                    }
                }

                // Draw signal line
                if (samples.size >= 2) {
                    val minTime = samples.first().timestamp.toFloat()
                    val maxTime = samples.last().timestamp.toFloat()
                    val timeRange = (maxTime - minTime).coerceAtLeast(1f)

                    val path = Path()
                    samples.forEachIndexed { index, sample ->
                        val x = chartLeft + chartWidth * ((sample.timestamp - minTime) / timeRange)
                        val rssiClamped = sample.rssi.toFloat().coerceIn(MIN_DBM, MAX_DBM)
                        val y = chartTop + chartHeight * (1f - (rssiClamped - MIN_DBM) / DBM_RANGE)

                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    // Draw line shadow
                    drawPath(
                        path = path,
                        color = Color.Black.copy(alpha = 0.1f),
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = path,
                        color = signalColor(latestRssi),
                        style = Stroke(
                            width = 3f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Draw current value dot
                    val lastSample = samples.last()
                    val lastX = chartRight
                    val lastRssiClamped = lastSample.rssi.toFloat().coerceIn(MIN_DBM, MAX_DBM)
                    val lastY = chartTop + chartHeight * (1f - (lastRssiClamped - MIN_DBM) / DBM_RANGE)

                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = signalColor(lastSample.rssi),
                        radius = 4f,
                        center = Offset(lastX, lastY)
                    )
                }

                // X-axis time labels
                if (samples.size >= 2) {
                    val now = samples.last().timestamp
                    val timeLabels = listOf("now", "30s", "1m", "2m", "5m")
                    val timeOffsets = listOf(0L, 30_000L, 60_000L, 120_000L, 300_000L)
                    val minTime = samples.first().timestamp
                    val maxTime = samples.last().timestamp
                    val totalDuration = maxTime - minTime

                    timeLabels.zip(timeOffsets).forEach { (label, offset) ->
                        if (offset <= totalDuration || offset == 0L) {
                            val targetTime = now - offset
                            val x = chartLeft + chartWidth * ((targetTime - minTime).toFloat() / totalDuration.coerceAtLeast(1L))
                            if (x in chartLeft..chartRight) {
                                val text = textMeasurer.measure(label, labelStyle)
                                drawText(
                                    textLayoutResult = text,
                                    topLeft = Offset(x - text.size.width / 2f, chartBottom + 4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawQualityBands(
    left: Float, top: Float, width: Float, height: Float
) {
    data class Band(val minDbm: Float, val maxDbm: Float, val color: Color)

    val bands = listOf(
        Band(-30f, -50f, ExcellentColor),
        Band(-50f, -60f, GoodColor),
        Band(-60f, -70f, FairColor),
        Band(-70f, -80f, PoorColor),
        Band(-80f, -90f, NoSignalColor)
    )

    bands.forEach { band ->
        val bandTop = top + height * (1f - (band.maxDbm - MIN_DBM) / DBM_RANGE)
        val bandBottom = top + height * (1f - (band.minDbm - MIN_DBM) / DBM_RANGE)
        drawRect(
            color = band.color.copy(alpha = 0.06f),
            topLeft = Offset(left, bandTop),
            size = androidx.compose.ui.geometry.Size(width, bandBottom - bandTop)
        )
    }
}

private fun signalColor(rssi: Int): Color = when {
    rssi >= -50 -> ExcellentColor
    rssi >= -60 -> GoodColor
    rssi >= -70 -> FairColor
    rssi >= -80 -> PoorColor
    else -> NoSignalColor
}
