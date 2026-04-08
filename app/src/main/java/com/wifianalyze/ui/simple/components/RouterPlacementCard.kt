package com.wifianalyze.ui.simple.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifianalyze.domain.model.RoomReading

@Composable
fun RouterPlacementCard(
    readings: List<RoomReading>,
    modifier: Modifier = Modifier
) {
    if (readings.size < 2) return

    val best  = readings.maxBy { it.rssi }
    val worst = readings.minBy { it.rssi }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Router,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Router Placement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PlacementRow(
                label = "Strongest signal",
                room = best.roomName,
                detail = "${best.rssi} dBm (${best.quality.label})",
                tint = best.quality.color
            )

            Spacer(modifier = Modifier.height(6.dp))

            PlacementRow(
                label = "Weakest signal",
                room = worst.roomName,
                detail = "${worst.rssi} dBm (${worst.quality.label})",
                tint = worst.quality.color
            )

            Spacer(modifier = Modifier.height(10.dp))

            val tip = buildTip(best, worst, readings)
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlacementRow(label: String, room: String, detail: String, tint: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(room, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun buildTip(best: RoomReading, worst: RoomReading, all: List<RoomReading>): String {
    val poorCount = all.count { it.rssi < -70 }
    return when {
        poorCount == 0 ->
            "Great coverage! Your router placement is working well across all tested rooms."
        poorCount == 1 ->
            "Signal is weak in ${worst.roomName}. Your router is likely near ${best.roomName} — try moving it toward ${worst.roomName} for better coverage."
        else ->
            "$poorCount rooms have poor signal. Consider moving your router away from ${best.roomName} toward a more central location, or add a WiFi extender."
    }
}
