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
    val gateway: String = "",
    val dnsServers: List<String> = emptyList(),
    val subnetPrefixLength: Int = 0,
    val txLinkSpeedMbps: Int = 0,
    val rxLinkSpeedMbps: Int = 0,
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
            gateway = "",
            dnsServers = emptyList(),
            subnetPrefixLength = 0,
            txLinkSpeedMbps = 0,
            rxLinkSpeedMbps = 0,
            isConnected = false
        )
    }
}
