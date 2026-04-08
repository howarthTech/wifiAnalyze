package com.wifianalyze.wear

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearSignalData(
    val ssid: String = "",
    val rssi: Int = 0,
    val quality: String = "—",
    val band: String = "",
    val isConnected: Boolean = false
)

object WearSignalState {
    private val _data = MutableStateFlow(WearSignalData())
    val data: StateFlow<WearSignalData> = _data.asStateFlow()

    fun update(ssid: String, rssi: Int, quality: String, band: String) {
        _data.value = WearSignalData(
            ssid = ssid,
            rssi = rssi,
            quality = quality,
            band = band,
            isConnected = ssid.isNotEmpty()
        )
    }
}
