package com.wifianalyze.domain.model

import androidx.compose.ui.graphics.Color

enum class CongestionLevel(val label: String, val color: Color) {
    LOW("Low", Color(0xFF4CAF50)),
    MEDIUM("Medium", Color(0xFFFFC107)),
    HIGH("High", Color(0xFFF44336))
}
