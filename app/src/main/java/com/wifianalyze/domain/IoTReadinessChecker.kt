package com.wifianalyze.domain

import com.wifianalyze.domain.model.CongestionLevel
import com.wifianalyze.domain.model.IoTReadiness
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IoTReadinessChecker @Inject constructor(
    private val congestionAnalyzer: CongestionAnalyzer
) {

    fun check(
        networks: List<WifiNetwork>,
        currentSsid: String
    ): IoTReadiness {
        val network24 = networks
            .filter { it.ssid == currentSsid && it.band == WifiBand.BAND_2_4_GHZ }
            .maxByOrNull { it.rssi }

        if (network24 == null) {
            return IoTReadiness(
                ready = false,
                reason = "No 2.4 GHz signal found for your network. Smart home devices need 2.4 GHz to connect."
            )
        }

        val signalOk = network24.rssi >= -70
        val congestion = congestionAnalyzer.analyzeCongestion(networks, currentSsid)
        val congestionOk = congestion != CongestionLevel.HIGH

        val qualityLabel = when {
            network24.rssi >= -50 -> "Excellent"
            network24.rssi >= -60 -> "Good"
            network24.rssi >= -70 -> "Fair"
            else -> "Weak"
        }

        return when {
            signalOk && congestionOk -> IoTReadiness(
                ready = true,
                reason = "Good for smart devices \u2014 2.4 GHz signal is $qualityLabel (Ch ${network24.channel})"
            )
            !signalOk -> IoTReadiness(
                ready = false,
                reason = "Smart devices may struggle \u2014 2.4 GHz signal is $qualityLabel. Move closer to router."
            )
            else -> IoTReadiness(
                ready = false,
                reason = "Smart devices may struggle \u2014 too many competing networks on 2.4 GHz"
            )
        }
    }
}
