package com.wifianalyze.domain.model

data class ConnectionInfo(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val linkSpeedMbps: Int,
    val frequency: Int,
    val channel: Int,
    val band: WifiBand,
    val ipAddress: String,
    val isConnected: Boolean
) {
    companion object {
        val DISCONNECTED = ConnectionInfo(
            ssid = "",
            bssid = "",
            rssi = -100,
            linkSpeedMbps = 0,
            frequency = 0,
            channel = 0,
            band = WifiBand.UNKNOWN,
            ipAddress = "",
            isConnected = false
        )
    }
}
