package com.wifianalyze.domain.model

data class RoomReading(
    val id: Long = 0,
    val roomName: String,
    val rssi: Int,
    val ssid: String,
    val frequency: Int,
    val channel: Int,
    val band: WifiBand,
    val quality: SignalQuality,
    val competingNetworks: Int,
    val congestionLevel: CongestionLevel,
    val iotReady: Boolean,
    val timestamp: Long
)
