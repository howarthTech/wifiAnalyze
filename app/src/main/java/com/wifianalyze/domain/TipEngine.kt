package com.wifianalyze.domain

import com.wifianalyze.domain.model.CongestionLevel
import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.SignalQuality
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipEngine @Inject constructor(
    private val signalMapper: SignalMapper,
    private val congestionAnalyzer: CongestionAnalyzer
) {

    fun generateTips(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<String> {
        if (!connection.isConnected) {
            return listOf("You're not connected to a WiFi network.")
        }

        val tips = mutableListOf<String>()
        val quality = signalMapper.mapSignalQuality(connection.rssi)
        val congestion = congestionAnalyzer.analyzeCongestion(networks, connection.ssid)

        when (quality) {
            SignalQuality.POOR, SignalQuality.NO_SIGNAL ->
                tips.add("Try moving closer to your router for a better signal.")
            SignalQuality.FAIR ->
                tips.add("Signal is okay but could be better. Moving closer to the router may help.")
            else -> {}
        }

        if (congestion == CongestionLevel.HIGH) {
            tips.add("This area has lots of competing networks. Changing your router's channel may help.")
        } else if (congestion == CongestionLevel.MEDIUM) {
            tips.add("There are a moderate number of nearby networks. Performance should still be fine.")
        }

        if (connection.band == WifiBand.BAND_5_GHZ || connection.band == WifiBand.BAND_6_GHZ) {
            tips.add("You're on the ${connection.band.label} band. Most smart home devices only work on 2.4 GHz.")
        }

        if (tips.isEmpty()) {
            tips.add("Your WiFi signal looks great here!")
        }

        return tips
    }
}
