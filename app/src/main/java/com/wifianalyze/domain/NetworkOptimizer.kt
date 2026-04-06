package com.wifianalyze.domain

import com.wifianalyze.domain.model.ConnectionInfo
import com.wifianalyze.domain.model.Recommendation
import com.wifianalyze.domain.model.Recommendation.Category
import com.wifianalyze.domain.model.Recommendation.Priority
import com.wifianalyze.domain.model.WifiBand
import com.wifianalyze.domain.model.WifiNetwork
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkOptimizer @Inject constructor() {

    fun analyze(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<Recommendation> {
        if (!connection.isConnected) return emptyList()

        val recommendations = mutableListOf<Recommendation>()

        recommendations.addAll(analyzeChannelCongestion(connection, networks))
        recommendations.addAll(analyzeSecurity(connection, networks))
        recommendations.addAll(analyzeBandUsage(connection, networks))
        recommendations.addAll(analyzeSignalStrength(connection))
        recommendations.addAll(analyzeEnvironment(connection, networks))

        return recommendations.sortedBy { it.priority.sortOrder }
    }

    private fun analyzeChannelCongestion(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        // Analyze 2.4 GHz channels
        val networks24 = networks.filter { it.band == WifiBand.BAND_2_4_GHZ }
        if (networks24.isNotEmpty()) {
            val myChannel24 = networks
                .filter { it.ssid == connection.ssid && it.band == WifiBand.BAND_2_4_GHZ }
                .maxByOrNull { it.rssi }
                ?.channel

            if (myChannel24 != null) {
                val bestChannel = findBest24Channel(networks24, myChannel24)
                if (bestChannel != null && bestChannel != myChannel24) {
                    val currentCount = countOverlapping24(networks24, myChannel24, connection.ssid)
                    val bestCount = countOverlapping24(networks24, bestChannel, connection.ssid)

                    if (currentCount > bestCount) {
                        val severity = if (currentCount >= 4) Priority.HIGH else Priority.MEDIUM
                        recs.add(
                            Recommendation(
                                title = "Switch 2.4 GHz to channel $bestChannel",
                                description = "Your 2.4 GHz is on channel $myChannel24 with $currentCount overlapping network${if (currentCount != 1) "s" else ""}. " +
                                        "Channel $bestChannel has only $bestCount. " +
                                        "Change this in your router settings (usually 192.168.1.1 or your router's app).",
                                priority = severity,
                                category = Category.CHANNEL
                            )
                        )
                    }
                }
            }
        }

        // Analyze 5 GHz channels
        val networks5 = networks.filter { it.band == WifiBand.BAND_5_GHZ }
        if (networks5.isNotEmpty()) {
            val myChannel5 = networks
                .filter { it.ssid == connection.ssid && it.band == WifiBand.BAND_5_GHZ }
                .maxByOrNull { it.rssi }
                ?.channel

            if (myChannel5 != null) {
                val sameChannelCount = networks5.count {
                    it.channel == myChannel5 && it.ssid != connection.ssid
                }
                if (sameChannelCount >= 2) {
                    // Find a less crowded 5 GHz channel
                    val channelCounts = networks5
                        .filter { it.ssid != connection.ssid }
                        .groupBy { it.channel }
                        .mapValues { it.value.size }

                    val leastUsedChannel = (36..165 step 4)
                        .filter { ch -> ch !in listOf(14) } // valid 5 GHz channels
                        .minByOrNull { channelCounts[it] ?: 0 }

                    if (leastUsedChannel != null && (channelCounts[leastUsedChannel] ?: 0) < sameChannelCount) {
                        recs.add(
                            Recommendation(
                                title = "Switch 5 GHz to channel $leastUsedChannel",
                                description = "Your 5 GHz channel $myChannel5 has $sameChannelCount other networks. " +
                                        "Channel $leastUsedChannel is less crowded.",
                                priority = Priority.MEDIUM,
                                category = Category.CHANNEL
                            )
                        )
                    }
                }
            }
        }

        return recs
    }

    private fun analyzeSecurity(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        val myNetworks = networks.filter { it.ssid == connection.ssid }
        if (myNetworks.isEmpty()) return recs

        val capabilities = myNetworks.first().capabilities

        when {
            capabilities.contains("WEP") -> {
                recs.add(
                    Recommendation(
                        title = "Upgrade from WEP security",
                        description = "Your network uses WEP encryption which is outdated and easily hacked. " +
                                "Switch to WPA2 or WPA3 in your router settings for much better security.",
                        priority = Priority.HIGH,
                        category = Category.SECURITY
                    )
                )
            }
            capabilities.contains("WPA") && !capabilities.contains("WPA2") && !capabilities.contains("WPA3") -> {
                recs.add(
                    Recommendation(
                        title = "Upgrade from WPA to WPA2/WPA3",
                        description = "Your network uses older WPA security. " +
                                "Upgrading to WPA2 or WPA3 in your router settings provides stronger protection.",
                        priority = Priority.MEDIUM,
                        category = Category.SECURITY
                    )
                )
            }
            capabilities.contains("WPA2") && !capabilities.contains("WPA3") -> {
                recs.add(
                    Recommendation(
                        title = "Consider upgrading to WPA3",
                        description = "Your network uses WPA2 which is good. WPA3 is the newest standard and offers even better security. " +
                                "Check if your router supports WPA2/WPA3 mixed mode.",
                        priority = Priority.LOW,
                        category = Category.SECURITY
                    )
                )
            }
        }

        // Check if any open networks are nearby (security awareness)
        val openNearby = networks
            .filter { it.ssid != connection.ssid && it.ssid.isNotBlank() }
            .filter { !it.capabilities.contains("WPA") && !it.capabilities.contains("WEP") }
            .distinctBy { it.ssid }

        if (openNearby.isNotEmpty()) {
            recs.add(
                Recommendation(
                    title = "${openNearby.size} open network${if (openNearby.size != 1) "s" else ""} nearby",
                    description = "Open WiFi networks have no password protection. " +
                            "Avoid connecting to them for banking or sensitive activities. " +
                            "Your network is secured, which is good.",
                    priority = Priority.LOW,
                    category = Category.SECURITY
                )
            )
        }

        return recs
    }

    private fun analyzeBandUsage(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        val myNetworks = networks.filter { it.ssid == connection.ssid }
        val has24 = myNetworks.any { it.band == WifiBand.BAND_2_4_GHZ }
        val has5 = myNetworks.any { it.band == WifiBand.BAND_5_GHZ }

        if (!has24 && has5) {
            recs.add(
                Recommendation(
                    title = "Enable 2.4 GHz band",
                    description = "Your router only broadcasts on 5 GHz here. Smart home devices, older laptops, " +
                            "and devices far from the router need 2.4 GHz. Enable it in your router settings.",
                    priority = Priority.HIGH,
                    category = Category.BAND
                )
            )
        }

        if (has24 && !has5) {
            recs.add(
                Recommendation(
                    title = "Enable 5 GHz band",
                    description = "Your router only broadcasts on 2.4 GHz. Adding the 5 GHz band gives faster speeds " +
                            "for streaming and downloads on devices near the router. Most modern routers support this.",
                    priority = Priority.MEDIUM,
                    category = Category.BAND
                )
            )
        }

        // Check if 2.4 GHz is very congested vs 5 GHz
        if (has24 && has5) {
            val competing24 = networks
                .filter { it.band == WifiBand.BAND_2_4_GHZ && it.ssid != connection.ssid && it.ssid.isNotBlank() }
                .distinctBy { it.ssid }
                .size
            val competing5 = networks
                .filter { it.band == WifiBand.BAND_5_GHZ && it.ssid != connection.ssid && it.ssid.isNotBlank() }
                .distinctBy { it.ssid }
                .size

            if (competing24 > 8 && competing5 < 3) {
                recs.add(
                    Recommendation(
                        title = "2.4 GHz band is very congested",
                        description = "$competing24 other networks on 2.4 GHz vs only $competing5 on 5 GHz. " +
                                "Connect phones, laptops, and streaming devices to 5 GHz when possible for better performance.",
                        priority = Priority.MEDIUM,
                        category = Category.BAND
                    )
                )
            }
        }

        return recs
    }

    private fun analyzeSignalStrength(
        connection: ConnectionInfo
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        if (connection.rssi in -80..-70) {
            recs.add(
                Recommendation(
                    title = "Consider a WiFi extender",
                    description = "Signal is fair in this location. A WiFi extender or mesh system " +
                            "could improve coverage here. Place it halfway between your router and this spot.",
                    priority = Priority.MEDIUM,
                    category = Category.PLACEMENT
                )
            )
        } else if (connection.rssi < -80) {
            recs.add(
                Recommendation(
                    title = "WiFi extender or mesh recommended",
                    description = "Signal is weak here. A mesh WiFi system or range extender " +
                            "would significantly improve coverage. Your router may also need repositioning " +
                            "— keep it in a central, elevated location away from walls and metal objects.",
                    priority = Priority.HIGH,
                    category = Category.PLACEMENT
                )
            )
        }

        if (connection.linkSpeedMbps in 1..72) {
            recs.add(
                Recommendation(
                    title = "Low connection speed: ${connection.linkSpeedMbps} Mbps",
                    description = "Your link speed is slow, which can cause buffering during video streaming. " +
                            "Moving closer to your router or switching to 5 GHz can increase speed.",
                    priority = Priority.MEDIUM,
                    category = Category.GENERAL
                )
            )
        }

        return recs
    }

    private fun analyzeEnvironment(
        connection: ConnectionInfo,
        networks: List<WifiNetwork>
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        // Check for common IoT devices broadcasting their own networks (setup mode)
        val iotBroadcasting = networks
            .filter { it.ssid != connection.ssid && it.ssid.isNotBlank() }
            .filter { net ->
                val lower = net.ssid.lowercase()
                lower.contains("setup") || lower.contains("config") ||
                        lower.contains("_setup") || lower.contains("-setup")
            }
            .distinctBy { it.ssid }

        if (iotBroadcasting.isNotEmpty()) {
            val names = iotBroadcasting.take(3).joinToString(", ") { "\"${it.ssid}\"" }
            recs.add(
                Recommendation(
                    title = "Device${if (iotBroadcasting.size > 1) "s" else ""} in setup mode nearby",
                    description = "$names appear${if (iotBroadcasting.size == 1) "s" else ""} to be a smart device waiting to be configured. " +
                            "Connect to its network from your phone's WiFi settings to set it up.",
                    priority = Priority.LOW,
                    category = Category.GENERAL
                )
            )
        }

        // Good news if everything looks optimal
        val myChannel = networks
            .filter { it.ssid == connection.ssid && it.band == WifiBand.BAND_2_4_GHZ }
            .maxByOrNull { it.rssi }
            ?.channel

        if (myChannel != null) {
            val overlapping = countOverlapping24(
                networks.filter { it.band == WifiBand.BAND_2_4_GHZ },
                myChannel,
                connection.ssid
            )
            if (overlapping == 0 && connection.rssi >= -60) {
                recs.add(
                    Recommendation(
                        title = "Your network is well optimized",
                        description = "No overlapping networks on your 2.4 GHz channel and strong signal. " +
                                "No changes needed!",
                        priority = Priority.LOW,
                        category = Category.GENERAL
                    )
                )
            }
        }

        return recs
    }

    /**
     * Find the best non-overlapping 2.4 GHz channel (1, 6, or 11 are the only non-overlapping ones).
     * Returns null if current channel is already best.
     */
    private fun findBest24Channel(
        networks: List<WifiNetwork>,
        currentChannel: Int
    ): Int? {
        val nonOverlapping = listOf(1, 6, 11)
        val channelScores = nonOverlapping.associateWith { targetChannel ->
            // Count networks that overlap with this channel
            // 2.4 GHz channels overlap within +/- 4 channels
            networks.count { network ->
                kotlin.math.abs(network.channel - targetChannel) <= 4
            }
        }

        val bestChannel = channelScores.minByOrNull { it.value }?.key ?: return null
        val currentScore = channelScores[currentChannel]
            ?: networks.count { kotlin.math.abs(it.channel - currentChannel) <= 4 }
        val bestScore = channelScores[bestChannel] ?: return null

        return if (bestScore < currentScore) bestChannel else null
    }

    /**
     * Count networks overlapping with a given 2.4 GHz channel (within +/- 4 channels).
     */
    private fun countOverlapping24(
        networks: List<WifiNetwork>,
        channel: Int,
        excludeSsid: String
    ): Int {
        return networks.count {
            it.ssid != excludeSsid &&
                    it.ssid.isNotBlank() &&
                    kotlin.math.abs(it.channel - channel) <= 4
        }
    }
}
