package com.wifianalyze.data.wifi

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.wifianalyze.domain.ChannelHelper
import com.wifianalyze.domain.model.ConnectionInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WifiAnalyze"

@Singleton
class ConnectionInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager
) {

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isLocationEnabled
    }

    private val _connectionInfo = MutableStateFlow(ConnectionInfo.DISCONNECTED)
    val connectionInfo: StateFlow<ConnectionInfo> = _connectionInfo.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startMonitoring() {
        try {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            // FLAG_INCLUDE_LOCATION_INFO is required on Android 12+ to get real SSID
            networkCallback = object : ConnectivityManager.NetworkCallback(
                ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO
            ) {
                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    try {
                        Log.d(TAG, "onCapabilitiesChanged: transportInfo=${capabilities.transportInfo}")
                        updateConnectionInfo(network, capabilities)
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException in onCapabilitiesChanged", e)
                        _connectionInfo.value = ConnectionInfo.DISCONNECTED
                    }
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Network lost")
                    _connectionInfo.value = ConnectionInfo.DISCONNECTED
                }
            }

            connectivityManager.registerNetworkCallback(request, networkCallback!!)
            refreshConnectionInfo()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in startMonitoring", e)
            _connectionInfo.value = ConnectionInfo.DISCONNECTED
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: IllegalArgumentException) {
                // Already unregistered
            }
            networkCallback = null
        }
    }

    fun refreshConnectionInfo() {
        try {
            val network = connectivityManager.activeNetwork ?: run {
                _connectionInfo.value = ConnectionInfo.DISCONNECTED
                return
            }
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: run {
                _connectionInfo.value = ConnectionInfo.DISCONNECTED
                return
            }
            updateConnectionInfo(network, capabilities)
        } catch (e: SecurityException) {
            _connectionInfo.value = ConnectionInfo.DISCONNECTED
        }
    }

    private fun updateConnectionInfo(network: Network, capabilities: NetworkCapabilities) {
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            _connectionInfo.value = ConnectionInfo.DISCONNECTED
            return
        }

        val wifiInfo = capabilities.transportInfo as? WifiInfo
        if (wifiInfo == null) {
            _connectionInfo.value = ConnectionInfo.DISCONNECTED
            return
        }

        val locationOn = isLocationEnabled()
        Log.d(TAG, "Location enabled: $locationOn")

        // The callback often redacts SSID to "<unknown ssid>" on Android 12+
        // Fall back to WifiManager.connectionInfo which respects location permission
        var ssid = wifiInfo.ssid?.removeSurrounding("\"") ?: ""
        var bssid = wifiInfo.bssid ?: ""
        Log.d(TAG, "Callback SSID: $ssid, BSSID: $bssid")

        if (ssid.isBlank() || ssid == "<unknown ssid>") {
            try {
                @Suppress("DEPRECATION")
                val legacyInfo = wifiManager.connectionInfo
                val legacySsid = legacyInfo?.ssid?.removeSurrounding("\"") ?: ""
                Log.d(TAG, "WifiManager fallback SSID: $legacySsid")
                if (legacySsid.isNotBlank() && legacySsid != "<unknown ssid>") {
                    ssid = legacySsid
                    bssid = legacyInfo?.bssid ?: bssid
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException getting WifiManager.connectionInfo", e)
            }
        }

        val freq = wifiInfo.frequency
        val networkDetails = getNetworkDetails(network)
        val ipAddress = networkDetails.ipAddress

        // TX/RX link speeds (API 30+)
        val txSpeed = wifiInfo.txLinkSpeedMbps
        val rxSpeed = wifiInfo.rxLinkSpeedMbps

        // Even if SSID is unknown, we're still connected if we have an IP and WiFi transport
        val isConnected = if (ssid.isNotBlank() && ssid != "<unknown ssid>") {
            true
        } else {
            // Connected but SSID hidden (location off) — still show as connected
            ipAddress.isNotBlank()
        }

        // If location is off and SSID unknown, show a helpful name
        if ((ssid.isBlank() || ssid == "<unknown ssid>") && ipAddress.isNotBlank()) {
            ssid = "WiFi (enable Location for name)"
            Log.d(TAG, "SSID unknown but connected — location likely off")
        }

        Log.d(TAG, "Final: ssid=$ssid rssi=${wifiInfo.rssi} freq=$freq ip=$ipAddress connected=$isConnected")

        _connectionInfo.value = ConnectionInfo(
            ssid = ssid,
            bssid = bssid,
            rssi = wifiInfo.rssi,
            linkSpeedMbps = wifiInfo.linkSpeed,
            frequency = freq,
            channel = ChannelHelper.frequencyToChannel(freq),
            band = ChannelHelper.frequencyToBand(freq),
            ipAddress = ipAddress,
            gateway = networkDetails.gateway,
            dnsServers = networkDetails.dnsServers,
            subnetPrefixLength = networkDetails.subnetPrefixLength,
            txLinkSpeedMbps = txSpeed,
            rxLinkSpeedMbps = rxSpeed,
            isConnected = isConnected
        )
    }

    private data class NetworkDetails(
        val ipAddress: String,
        val gateway: String,
        val dnsServers: List<String>,
        val subnetPrefixLength: Int
    )

    private fun getNetworkDetails(network: Network): NetworkDetails {
        return try {
            val linkProperties = connectivityManager.getLinkProperties(network)

            val ipAddress = linkProperties?.linkAddresses
                ?.firstOrNull { it.address is Inet4Address }
                ?.address
                ?.hostAddress ?: ""

            val subnetPrefix = linkProperties?.linkAddresses
                ?.firstOrNull { it.address is Inet4Address }
                ?.prefixLength ?: 0

            val gateway = linkProperties?.routes
                ?.firstOrNull { it.isDefaultRoute }
                ?.gateway
                ?.hostAddress ?: ""

            val dnsServers = linkProperties?.dnsServers
                ?.mapNotNull { it.hostAddress }
                ?: emptyList()

            NetworkDetails(ipAddress, gateway, dnsServers, subnetPrefix)
        } catch (e: SecurityException) {
            NetworkDetails("", "", emptyList(), 0)
        }
    }
}
