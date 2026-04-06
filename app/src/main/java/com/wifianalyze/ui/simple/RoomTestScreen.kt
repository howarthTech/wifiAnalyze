package com.wifianalyze.ui.simple

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifianalyze.ui.simple.components.IoTReadinessCard
import com.wifianalyze.ui.simple.components.SignalQualityCard

@OptIn(ExperimentalMaterial3Api::class)
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
                title = { Text("Test This Room") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignalQualityCard(
                quality = state.quality,
                signalPercent = state.signalPercent,
                signalColor = state.signalColor,
                isConnected = state.isConnected
            )

            IoTReadinessCard(readiness = state.iotReadiness)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Name this location to save the reading",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = roomName,
                onValueChange = {
                    roomName = it
                    saved = false
                },
                label = { Text("Room name") },
                placeholder = { Text("e.g., Kitchen, Garage, Bedroom") },
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (saved) "Saved!" else "Save Reading")
            }

            if (saved) {
                Text(
                    text = "Reading saved for \"$roomName\". Go back or test another room.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
