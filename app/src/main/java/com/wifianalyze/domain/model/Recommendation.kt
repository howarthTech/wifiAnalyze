package com.wifianalyze.domain.model

import androidx.compose.ui.graphics.Color

data class Recommendation(
    val title: String,
    val description: String,
    val priority: Priority,
    val category: Category
) {
    enum class Priority(val sortOrder: Int, val color: Color) {
        HIGH(0, Color(0xFFF44336)),
        MEDIUM(1, Color(0xFFFFC107)),
        LOW(2, Color(0xFF2196F3))
    }

    enum class Category {
        CHANNEL,
        SECURITY,
        BAND,
        PLACEMENT,
        GENERAL
    }
}
