package com.wifianalyze.ui.simple.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wifianalyze.domain.model.IoTReadiness
import com.wifianalyze.ui.theme.SignalExcellent
import com.wifianalyze.ui.theme.SignalPoor

@Composable
fun IoTReadinessCard(
    readiness: IoTReadiness,
    modifier: Modifier = Modifier
) {
    val color = if (readiness.ready) SignalExcellent else SignalPoor
    val icon = if (readiness.ready) Icons.Rounded.CheckCircle else Icons.Rounded.Warning

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = readiness.reason,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
