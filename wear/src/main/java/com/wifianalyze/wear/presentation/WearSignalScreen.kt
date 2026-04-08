package com.wifianalyze.wear.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.wifianalyze.wear.WearSignalData

@Composable
fun WearSignalScreen(data: WearSignalData) {
    val signalColor = qualityColor(data.quality)
    val progress = if (data.isConnected) rssiToProgress(data.rssi) else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        // Arc progress indicator around the edge
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            indicatorColor = signalColor,
            trackColor = Color.White.copy(alpha = 0.12f),
            strokeWidth = 6.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (data.isConnected) {
                Text(
                    text = data.quality,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = signalColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${data.rssi} dBm",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = data.ssid.take(16),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                if (data.band.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.band,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "No WiFi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun rssiToProgress(rssi: Int): Float {
    // Map rssi -30 (best) to -100 (worst) → 1.0 to 0.0
    return ((rssi - (-100f)) / (-30f - (-100f))).coerceIn(0f, 1f)
}

private fun qualityColor(quality: String): Color = when (quality) {
    "Excellent" -> Color(0xFF4CAF50)
    "Good"      -> Color(0xFF8BC34A)
    "Fair"      -> Color(0xFFFFEB3B)
    "Poor"      -> Color(0xFFFF9800)
    else        -> Color(0xFFF44336)
}
