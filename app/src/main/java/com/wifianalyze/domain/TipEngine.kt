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
            return listOf("Connect to your WiFi network to check signal strength and coverage.")
        }

        val tips = mutableListOf<String>()
        val quality = signalMapper.mapSignalQuality(connection.rssi)
        val congestion = congestionAnalyzer.analyzeCongestion(networks, connection.ssid)

        // Signal strength tips
        when (quality) {
            SignalQuality.POOR, SignalQuality.NO_SIGNAL ->
                tips.add("Signal is weak here. Try moving closer to your router, or consider a WiFi extender for this area.")
            SignalQuality.FAIR ->
                tips.add("Signal is usable but not strong. Walls and floors between you and the router reduce signal.")
            SignalQuality.EXCELLENT ->
                tips.add("Signal is excellent here — great spot for any device!")
            else -> {}
        }

        // Congestion tips
        if (congestion == CongestionLevel.HIGH) {
            tips.add("Lots of nearby networks competing for airspace. Changing your router's WiFi channel in its settings could help.")
        } else if (congestion == CongestionLevel.MEDIUM) {
            tips.add("Moderate number of nearby networks. Performance should be fine unless you notice slowdowns.")
        }

        // IoT-specific band tip
        val has24 = networks.any { it.ssid == connection.ssid && it.band == WifiBand.BAND_2_4_GHZ }
        if (connection.band == WifiBand.BAND_5_GHZ || connection.band == WifiBand.BAND_6_GHZ) {
            if (has24) {
                tips.add("Your phone is on ${connection.band.label} (faster), but smart home devices use the 2.4 GHz signal shown in the bands section above.")
            } else {
                tips.add("No 2.4 GHz signal detected. Check your router settings — smart home devices need 2.4 GHz enabled.")
            }
        }

        // Link speed tip
        if (connection.linkSpeedMbps in 1..49) {
            tips.add("Connection speed is low (${connection.linkSpeedMbps} Mbps). Video streaming may buffer at this speed.")
        }

        if (tips.isEmpty()) {
            tips.add("Your WiFi looks great here — all good!")
        }

        return tips
    }
}
