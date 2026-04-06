package com.wifianalyze.domain.model

data class BandSignalInfo(
    val band: WifiBand,
    val rssi: Int,
    val quality: SignalQuality,
    val channel: Int,
    val isConnectedBand: Boolean
)
