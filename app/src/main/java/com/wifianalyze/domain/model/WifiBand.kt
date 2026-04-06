package com.wifianalyze.domain.model

enum class WifiBand(val label: String, val simpleLabel: String) {
    BAND_2_4_GHZ("2.4 GHz", "Longer-range signal (2.4 GHz)"),
    BAND_5_GHZ("5 GHz", "Faster signal (5 GHz)"),
    BAND_6_GHZ("6 GHz", "Fastest signal (6 GHz)"),
    UNKNOWN("Unknown", "Unknown band")
}
