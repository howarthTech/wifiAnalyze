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
        description = "Perfect for all devices",
        color = Color(0xFF4CAF50),
        sortOrder = 0
    ),
    GOOD(
        label = "Good",
        description = "Great for most devices",
        color = Color(0xFF8BC34A),
        sortOrder = 1
    ),
    FAIR(
        label = "Fair",
        description = "May work for basic devices",
        color = Color(0xFFFFC107),
        sortOrder = 2
    ),
    POOR(
        label = "Poor",
        description = "Devices will struggle here",
        color = Color(0xFFFF9800),
        sortOrder = 3
    ),
    NO_SIGNAL(
        label = "No Signal",
        description = "Not usable",
        color = Color(0xFFF44336),
        sortOrder = 4
    )
}
