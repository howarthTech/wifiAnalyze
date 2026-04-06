package com.wifianalyze.domain

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.wifianalyze.domain.model.SignalQuality
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalMapper @Inject constructor() {

    fun mapSignalQuality(rssi: Int): SignalQuality = when {
        rssi >= -50 -> SignalQuality.EXCELLENT
        rssi >= -60 -> SignalQuality.GOOD
        rssi >= -70 -> SignalQuality.FAIR
        rssi >= -80 -> SignalQuality.POOR
        else -> SignalQuality.NO_SIGNAL
    }

    fun signalColor(rssi: Int): Color {
        val clamped = rssi.coerceIn(-90, -40)
        val fraction = (clamped + 90f) / 50f
        return lerp(SignalQuality.NO_SIGNAL.color, SignalQuality.EXCELLENT.color, fraction)
    }

    fun signalPercentage(rssi: Int): Float {
        val clamped = rssi.coerceIn(-90, -40)
        return ((clamped + 90f) / 50f).coerceIn(0f, 1f)
    }
}
