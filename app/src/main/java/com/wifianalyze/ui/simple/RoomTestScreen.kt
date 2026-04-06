package com.wifianalyze.ui.simple

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.ui.simple.components.IoTReadinessCard
import com.wifianalyze.ui.simple.components.SignalQualityCard
import com.wifianalyze.ui.theme.SignalExcellent

private val ROOM_SUGGESTIONS = listOf(
    "Living Room", "Kitchen", "Bedroom", "Bathroom",
    "Garage", "Basement", "Attic", "Office",
    "Patio", "Backyard", "Front Porch", "Hallway"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomTestScreen(
    onNavigateBack: () -> Unit,
    viewModel: SimpleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var roomName by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test This Spot") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SignalQualityCard(
                quality = state.quality,
                signalPercent = state.signalPercent,
                signalColor = state.signalColor,
                isConnected = state.isConnected,
                isScanning = state.isScanning
            )

            IoTReadinessCard(readiness = state.iotReadiness)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Name this spot",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Quick pick room suggestions
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ROOM_SUGGESTIONS.forEach { suggestion ->
                    FilterChip(
                        selected = roomName == suggestion,
                        onClick = {
                            roomName = suggestion
                            saved = false
                        },
                        label = { Text(suggestion, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            OutlinedTextField(
                value = roomName,
                onValueChange = {
                    roomName = it
                    saved = false
                },
                label = { Text("Or type a custom name") },
                placeholder = { Text("e.g., Master Bedroom, Back Deck") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (roomName.isNotBlank()) {
                        viewModel.saveRoomReading(roomName.trim())
                        saved = true
                    }
                },
                enabled = roomName.isNotBlank() && state.isConnected && !saved,
                modifier = Modifier.fillMaxWidth(),
                colors = if (saved) ButtonDefaults.buttonColors(
                    containerColor = SignalExcellent
                ) else ButtonDefaults.buttonColors()
            ) {
                if (saved) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(if (saved) "Saved!" else "Save Reading")
            }

            AnimatedVisibility(
                visible = saved,
                enter = fadeIn() + slideInVertically()
            ) {
                Text(
                    text = "Saved \"$roomName\"! Tap another room name to test more spots, or go back to the dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
