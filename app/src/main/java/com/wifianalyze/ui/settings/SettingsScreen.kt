package com.wifianalyze.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LocalPizza
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isAdvancedMode: Boolean,
    isDarkMode: Boolean,
    alertsEnabled: Boolean,
    alertThresholdDbm: Int,
    onNavigateBack: () -> Unit,
    onClearData: () -> Unit,
    onModeChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onAlertsEnabledChanged: (Boolean) -> Unit,
    onAlertThresholdChanged: (Int) -> Unit
) {
    val context = LocalContext.current

    var sliderValue by remember(alertThresholdDbm) { mutableFloatStateOf(alertThresholdDbm.toFloat()) }

    val hasNotifPermission = remember(alertsEnabled) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* user responded — NotificationHelper checks permission on send */ }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Display ───────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Display", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleRow(
                        title = if (isAdvancedMode) "Advanced Mode" else "Simple Mode",
                        subtitle = if (isAdvancedMode) "Technical details, dBm, charts, latency" else "Easy to read signal quality and IoT tips",
                        checked = isAdvancedMode,
                        onCheckedChange = onModeChanged
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ToggleRow(
                        title = "Dark Mode",
                        subtitle = "Force dark theme regardless of system setting",
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeChanged
                    )
                }
            }

            // ── Alerts ────────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Signal Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    ToggleRow(
                        title = "Enable Alerts",
                        subtitle = "Notify when signal drops below threshold",
                        checked = alertsEnabled,
                        onCheckedChange = { enabled ->
                            onAlertsEnabledChanged(enabled)
                            if (enabled && !hasNotifPermission) {
                                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )

                    if (alertsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Threshold", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${sliderValue.toInt()} dBm",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onAlertThresholdChanged(sliderValue.toInt()) },
                            valueRange = -90f..-50f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Alert fires once per minute while signal stays below threshold.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (alertsEnabled && !hasNotifPermission) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notification permission not granted. Enable it in system Settings → Apps.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // ── Data ──────────────────────────────────────────────────────────
            if (!isAdvancedMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onClearData) {
                            Text("Clear All Saved Rooms")
                        }
                    }
                }
            }

            // ── Support ───────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "If you find this app useful, consider buying me a slice!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/howarthtech"))
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFFE8430A),
                            contentColor = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalPizza,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Buy Me a Pizza", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("WiFi Analyze v1.1", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Check WiFi signal strength and find the best spots for your smart home devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://howarthtech.github.io/wifiAnalyze/store/privacy-policy.html"))
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Privacy Policy")
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
