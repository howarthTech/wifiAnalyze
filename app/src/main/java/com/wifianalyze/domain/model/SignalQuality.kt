package com.wifianalyze.domain.model

import androidx.compose.ui.graphics.Color

enum class SignalQuality(
    val label: String,
    val description: String,
    val color: Color,
    val sortOrder: Int
) {
    EXCELLENT(
        label = "Excellent",
        description = "Perfect for all devices — streaming, gaming, smart home",
        color = Color(0xFF4CAF50),
        sortOrder = 0
    ),
    GOOD(
        label = "Good",
        description = "Works well for most devices and activities",
        color = Color(0xFF8BC34A),
        sortOrder = 1
    ),
    FAIR(
        label = "Fair",
        description = "Basic browsing OK, but smart devices may drop out",
        color = Color(0xFFFFC107),
        sortOrder = 2
    ),
    POOR(
        label = "Poor",
        description = "Devices will struggle — consider a WiFi extender",
        color = Color(0xFFFF9800),
        sortOrder = 3
    ),
    NO_SIGNAL(
        label = "No Signal",
        description = "Too far from router — devices won't connect here",
        color = Color(0xFFF44336),
        sortOrder = 4
    )
}
