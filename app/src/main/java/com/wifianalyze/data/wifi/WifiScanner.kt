package com.wifianalyze.data.wifi

import com.wifianalyze.domain.model.WifiNetwork
import kotlinx.coroutines.flow.StateFlow

interface WifiScanner {
    val scanResults: StateFlow<List<WifiNetwork>>
    val isScanning: StateFlow<Boolean>
    fun startScan()
    fun stopScanning()
}
