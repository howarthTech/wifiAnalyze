package com.wifianalyze.domain

import com.wifianalyze.domain.model.WifiBand

object ChannelHelper {

    fun frequencyToChannel(freqMhz: Int): Int = when (freqMhz) {
        in 2412..2472 -> (freqMhz - 2407) / 5
        2484 -> 14
        in 5170..5825 -> (freqMhz - 5000) / 5
        in 5955..7115 -> (freqMhz - 5950) / 5
        else -> 0
    }

    fun frequencyToBand(freqMhz: Int): WifiBand = when (freqMhz) {
        in 2400..2500 -> WifiBand.BAND_2_4_GHZ
        in 5000..5900 -> WifiBand.BAND_5_GHZ
        in 5925..7125 -> WifiBand.BAND_6_GHZ
        else -> WifiBand.UNKNOWN
    }
}
