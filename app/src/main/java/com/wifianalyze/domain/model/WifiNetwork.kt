package com.wifianalyze.domain.model

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val band: WifiBand,
    val capabilities: String,
    val channelWidthMhz: Int = 20,
    val vendorName: String = ""
)
