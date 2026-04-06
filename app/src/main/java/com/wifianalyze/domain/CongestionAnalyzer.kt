package com.wifianalyze.domain

import com.wifianalyze.domain.model.CongestionLevel
import com.wifianalyze.domain.model.WifiNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CongestionAnalyzer @Inject constructor() {

    fun countCompetingNetworks(
        networks: List<WifiNetwork>,
        currentSsid: String
    ): Int = networks
        .filter { it.ssid != currentSsid && it.ssid.isNotBlank() }
        .distinctBy { it.ssid }
        .size

    fun analyzeCongestion(
        networks: List<WifiNetwork>,
        currentSsid: String
    ): CongestionLevel {
        val count = countCompetingNetworks(networks, currentSsid)
        return when {
            count <= 5 -> CongestionLevel.LOW
            count <= 15 -> CongestionLevel.MEDIUM
            else -> CongestionLevel.HIGH
        }
    }
}
