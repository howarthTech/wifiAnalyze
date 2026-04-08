package com.wifianalyze.ui.advanced.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifianalyze.domain.model.SignalQuality
import com.wifianalyze.domain.model.WifiBand

@Composable
fun RawSignalCard(
    rssi: Int,
    quality: SignalQuality,
    signalColor: Color,
    frequency: Int,
    channel: Int,
    band: WifiBand,
    linkSpeedMbps: Int,
    bssid: String,
    vendorName: String,
    isConnected: Boolean,
    isInitializing: Boolean,
    stabilityScore: Int? = null,
    stabilityLabel: String = "",
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = signalColor,
        animationSpec = tween(500),
        label = "signalColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scanAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanPulse"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isInitializing)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                animatedColor.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (isInitializing) {
                Text(
                    text = "Scanning...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alpha(scanAlpha)
                )
                Text(
                    text = "Checking your WiFi signal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!isConnected) {
                Text(
                    text = "Not Connected",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
            } else {
                // Large dBm display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$rssi",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Text(
                            text = "dBm",
                            style = MaterialTheme.typography.titleMedium,
                            color = animatedColor
                        )
                        Text(
                            text = quality.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = animatedColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Frequency", "${frequency} MHz")
                    DetailItem("Channel", "$channel")
                    DetailItem("Band", band.label)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Link Speed", "${linkSpeedMbps} Mbps")
                    DetailItem("BSSID", bssid)
                    DetailItem("Vendor", vendorName.ifEmpty { "—" })
                }

                if (stabilityScore != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val stabilityColor = when {
                        stabilityScore >= 85 -> Color(0xFF4CAF50)
                        stabilityScore >= 65 -> Color(0xFF8BC34A)
                        stabilityScore >= 45 -> Color(0xFFFFC107)
                        else                 -> Color(0xFFFF9800)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailItem("Stability", stabilityLabel)
                        Text(
                            text = "$stabilityScore / 100",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = stabilityColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
