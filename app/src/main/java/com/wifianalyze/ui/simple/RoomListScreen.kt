package com.wifianalyze.ui.simple

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.domain.model.RoomReading
import com.wifianalyze.ui.simple.components.RoomReadingItem
import com.wifianalyze.ui.simple.components.RouterPlacementCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    onNavigateBack: () -> Unit,
    viewModel: SimpleViewModel = hiltViewModel()
) {
    val readings by viewModel.savedReadings.collectAsState(initial = emptyList())
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Rooms") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (readings.isNotEmpty()) {
                        IconButton(onClick = {
                            val report = buildRoomReport(readings)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                                putExtra(Intent.EXTRA_SUBJECT, "WiFi Room Report")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Room Report"))
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share report")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (readings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Text(
                    text = "No rooms tested yet",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Walk to different spots and tap \"Test Room\" to save signal readings. Compare rooms to find the best spots for your devices.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .animateContentSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    RouterPlacementCard(
                        readings = readings,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "Ranked by signal strength (best first). Swipe to delete.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(items = readings, key = { it.id }) { reading ->
                    SwipeToDismissItem(
                        reading = reading,
                        onDismiss = { viewModel.deleteReading(reading) }
                    )
                }
            }
        }
    }
}

private fun buildRoomReport(readings: List<RoomReading>): String {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    return buildString {
        appendLine("WiFi Room Signal Report")
        appendLine("Generated: $dateStr")
        appendLine("=".repeat(40))
        readings.forEachIndexed { i, r ->
            appendLine()
            appendLine("${i + 1}. ${r.roomName}")
            appendLine("   Signal : ${r.quality.label} (${r.rssi} dBm)")
            appendLine("   Network: ${r.ssid}")
            appendLine("   Band   : ${r.band.label}  Channel: ${r.channel}")
            appendLine("   IoT    : ${if (r.iotReady) "Ready" else "Not ready"}")
            appendLine("   Congestion: ${r.congestionLevel.name.lowercase().replaceFirstChar { it.uppercase() }}")
        }
        appendLine()
        val best = readings.maxBy { it.rssi }
        val worst = readings.minBy { it.rssi }
        appendLine("=".repeat(40))
        appendLine("Best  : ${best.roomName} (${best.rssi} dBm)")
        appendLine("Worst : ${worst.roomName} (${worst.rssi} dBm)")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissItem(
    reading: RoomReading,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onDismiss()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        RoomReadingItem(reading = reading)
    }
}
