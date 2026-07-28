package com.wifianalyze.ui.simple.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Shown when WiFi or Location Services are off — the two states where the app
 * silently can't work and a non-technical user has no way to know why.
 */
@Composable
fun StatusNoticeCard(
    wifiEnabled: Boolean,
    locationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val (icon, title, message, buttonLabel, settingsAction) = when {
        !wifiEnabled -> NoticeContent(
            icon = Icons.Rounded.WifiOff,
            title = "WiFi is turned off",
            message = "Turn on WiFi to see your signal and nearby networks.",
            buttonLabel = "Turn On WiFi",
            settingsAction = Settings.Panel.ACTION_WIFI
        )
        !locationEnabled -> NoticeContent(
            icon = Icons.Rounded.LocationOff,
            title = "Location is turned off",
            message = "Android requires Location Services to be on to show WiFi network names and scan results. Your location is never stored or shared.",
            buttonLabel = "Turn On Location",
            settingsAction = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        )
        else -> return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(settingsAction))
                    } catch (_: Exception) {
                        // Fall back to the full settings app if the panel is unavailable
                        try {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS))
                        } catch (_: Exception) { /* nothing sensible left to do */ }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }
        }
    }
}

private data class NoticeContent(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val buttonLabel: String,
    val settingsAction: String
)
