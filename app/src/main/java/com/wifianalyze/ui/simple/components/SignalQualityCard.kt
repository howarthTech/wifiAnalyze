package com.wifianalyze.ui.simple.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SignalWifi0Bar
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material.icons.rounded.SignalWifiStatusbar4Bar
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifianalyze.domain.model.SignalQuality

@Composable
fun SignalQualityCard(
    quality: SignalQuality,
    signalPercent: Float,
    signalColor: Color,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = signalColor,
        animationSpec = tween(500),
        label = "signalColor"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = signalPercent,
        animationSpec = tween(500),
        label = "signalProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = animatedColor.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = wifiIcon(quality, isConnected),
                contentDescription = quality.label,
                modifier = Modifier.size(72.dp),
                tint = animatedColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isConnected) quality.label else "Not Connected",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isConnected) quality.description else "Connect to a WiFi network to start",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = animatedColor,
                    trackColor = animatedColor.copy(alpha = 0.2f),
                )
            }
        }
    }
}

private fun wifiIcon(quality: SignalQuality, isConnected: Boolean): ImageVector {
    if (!isConnected) return Icons.Rounded.WifiOff
    return when (quality) {
        SignalQuality.EXCELLENT -> Icons.Rounded.SignalWifi4Bar
        SignalQuality.GOOD -> Icons.Rounded.SignalWifiStatusbar4Bar
        SignalQuality.FAIR -> Icons.Rounded.Wifi
        SignalQuality.POOR -> Icons.Rounded.SignalWifi0Bar
        SignalQuality.NO_SIGNAL -> Icons.Rounded.WifiOff
    }
}
